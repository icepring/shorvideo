package com.tym.shortvideo.recodrender;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES30;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.tym.shortvideo.utils.Size;
import android.view.SurfaceHolder;


import com.tym.shortvideo.filter.helper.gles.EglCore;
import com.tym.shortvideo.filter.helper.gles.WindowSurface;
import com.tym.shortvideo.utils.CameraInfo;
import com.tym.shortvideo.utils.CameraUtils;
import com.tym.shortvideo.filter.helper.type.GLFilterGroupType;
import com.tym.shortvideo.filter.helper.type.GLFilterType;
import com.tym.shortvideo.filter.helper.type.GlUtil;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * 渲染线程
 * Created by cain on 2017/11/4.
 */

public class RenderThread extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener,
        Camera.PreviewCallback{

    private static final String TAG = "RenderThread";

    private boolean isDebug = false;
    // 操作锁
    private final Object mSynOperation = new Object();
    // Looping锁
    private final Object mSyncIsLooping = new Object();

    private boolean isPreviewing = false;   // 是否预览状态
    private boolean isRecording = false;    // 是否录制状态
    private boolean isRecordingPause = false;   // 是否处于暂停录制状态

    // EGL共享上下文
    private EglCore mEglCore;
    // 预览用的EGLSurface
    private WindowSurface mDisplaySurface;

    // CameraTexture对应的Id
    private int mCameraTextureId;
    private SurfaceTexture mCameraTexture;

    // 矩阵
    private final float[] mMatrix = new float[16];

    // 视图宽高
    private int mViewWidth, mViewHeight;
    // 预览图片大小
    private int mImageWidth, mImageHeight;

    // 更新帧的锁
    private final Object mSyncFrameNum = new Object();
    private final Object mSyncTexture = new Object();
    // 可用帧
    private int mFrameNum = 0;
    // 拍照
    private boolean isTakePicture = false;
    // 拍照回调
    private CaptureFrameCallback mCaptureFrameCallback;

    // 预览回调缓存，解决previewCallback回调内存抖动问题
    private byte[] mPreviewBuffer;

    // 渲染状态回调
    private RenderStateChangedListener mRenderStateListener;
    private RenderHandler mRenderHandler;

    // 计算帧率
    private FrameRateMeter mFrameRateMeter;
    private WeakReference<Handler> mWeakFpsHandler;

    private Context mContext;

    public RenderThread(Context context, String name) {
        super(name);
        mContext = context;
    }

    public void setRenderHandler(RenderHandler handler) {
        mRenderHandler = handler;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        addNewFrame();
    }

    private long time = 0;
    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        if (mRenderHandler != null) {
            synchronized (mSynOperation) {
                if (isPreviewing || isRecording) {
                    mRenderHandler.sendMessage(mRenderHandler
                            .obtainMessage(RenderHandler.MSG_PREVIEW_CALLBACK, data));
                }
            }
        }
        if (mPreviewBuffer != null) {
            camera.addCallbackBuffer(mPreviewBuffer);
        }
        if (isDebug) {
            Log.d("onPreviewFrame", "update time = " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void surfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();
        mCameraTextureId = GlUtil.createTextureOES();
        mCameraTexture = new SurfaceTexture(mCameraTextureId);
        mCameraTexture.setOnFrameAvailableListener(this);
        // 打开相机
        CameraUtils.openCamera(mContext, CameraUtils.DESIRED_PREVIEW_FPS);
        // 设置预览Surface
        CameraUtils.setPreviewSurface(mCameraTexture);
        calculateImageSize();

        // 渲染初始化
        RenderManager.getInstance().init();
        RenderManager.getInstance().onInputSizeChanged(mImageWidth, mImageHeight);

        // 禁用深度测试和背面绘制
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        // 添加预览回调以及回调buffer，用于人脸检测
        initPreviewCallback();
        // 初始化人脸检测工具
        initFaceDetection();
    }

    void surfaceChanged(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        onFilterChanged();
        RenderManager.getInstance().updateTextureBuffer();
        RenderManager.getInstance().onDisplaySizeChanged(mViewWidth, mViewHeight);
        // 开始预览
        CameraUtils.startPreview();
        isPreviewing = true;
        // 渲染状态回调
        if (mRenderStateListener != null) {
            mRenderStateListener.onPreviewing(isPreviewing);
        }

    }

    void surfaceDestoryed() {
        isPreviewing = false;
        // 渲染状态回调
        if (mRenderStateListener != null) {
            mRenderStateListener.onPreviewing(isPreviewing);
        }
        if (mWeakFpsHandler != null) {
            mWeakFpsHandler.clear();
            mWeakFpsHandler = null;
        }

        // 释放回调，否则会提示 camera handler回调到一个dead thread的出错信息
        CameraUtils.setPreviewCallbackWithBuffer(null, null);
        // 释放相机
        CameraUtils.releaseCamera();
        // 释放Filter(需要在EGLContext释放之前处理，否则会报以下错误：
        // E/libEGL: call to OpenGL ES API with no current context (logged once per thread)
        RenderManager.getInstance().release();
        if (mCameraTexture != null) {
            mCameraTexture.release();
            mCameraTexture = null;
        }
        if (mDisplaySurface != null) {
            mDisplaySurface.release();
            mDisplaySurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    /**
     * 初始化预览回调
     * 备注：在某些设备上，需要在setPreviewTexture之后，startPreview之前添加回调才能使得onPreviewFrame回调正常
     */
    private void initPreviewCallback() {
        Size previewSize = CameraUtils.getPreviewSize();
        int size = previewSize.getWidth() * previewSize.getHeight() * 3 / 2;
        mPreviewBuffer = new byte[size];
        CameraUtils.setPreviewCallbackWithBuffer(this, mPreviewBuffer);
    }

    /**
     * 初始化人脸检测工具
     */
    private void initFaceDetection() {

    }

    /**
     * 预览回调
     * @param data
     */
    void onPreviewCallback(byte[] data) {
        // 如果允许关键点检测，则进入关键点检测阶段，否则立即更新帧

    }

    /**
     * 开始预览
     */
    void startPreview() {
        if (mCameraTexture != null) {
            RenderManager.getInstance().updateTextureBuffer();
            CameraUtils.setPreviewSurface(mCameraTexture);
            initPreviewCallback();
            CameraUtils.startPreview();
            isPreviewing = true;
            if (mRenderStateListener != null) {
                mRenderStateListener.onPreviewing(isPreviewing);
            }
        }
    }

    /**
     * 停止预览
     */
    void stopPreview() {
        isPreviewing = false;
        CameraUtils.stopPreview();
        if (mRenderStateListener != null) {
            mRenderStateListener.onPreviewing(isPreviewing);
        }
    }

    /**
     * 计算imageView 的宽高
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void calculateImageSize() {
        Size size = CameraUtils.getPreviewSize();
        CameraInfo info = CameraUtils.getCameraInfo();
        if (info != null) {
            if (info.getOrientation() == 90 || info.getOrientation() == 270) {
                mImageWidth = size.getHeight();
                mImageHeight = size.getWidth();
            } else {
                mImageWidth = size.getWidth();
                mImageHeight = size.getHeight();
            }
        }
    }


    /**
     * 设置对焦区域
     * @param rect
     */
    void setFocusAres(Rect rect) {
        CameraUtils.setFocusArea(rect);
    }

    /**
     * 设置打开闪光灯
     * @param on
     */
    void setFlashLight(boolean on) {
        CameraUtils.setFlashLight(on);
    }

    /**
     * 滤镜或视图发生变化时调用
     */
    private void onFilterChanged() {
        RenderManager.getInstance().onFilterChanged();
    }

    /**
     * 设置美颜等级(百分比)
     * @param percent 0 ~ 100
     */
    void setBeautifyLevel(int percent) {
        RenderManager.getInstance().setBeautifyLevel(percent);
    }

    /**
     * 更新filter
     * @param type Filter类型
     */
    void changeFilter(GLFilterType type) {
        RenderManager.getInstance().changeFilter(type);
    }

    /**
     * 切换滤镜组
     * @param type
     */
    void changeFilterGroup(GLFilterGroupType type) {
        synchronized (mSyncIsLooping) {
            RenderManager.getInstance().changeFilterGroup(type);
        }
    }

    private long temp = 0;
    /**
     * 绘制帧
     */
    void drawFrame() {
        temp = System.currentTimeMillis();
        // 如果存在新的帧，则更新帧
        synchronized (mSyncFrameNum) {
            synchronized (mSyncTexture) {
                if (mCameraTexture != null) {
                    while (mFrameNum != 0) {
                        mCameraTexture.updateTexImage();
                        --mFrameNum;
                    }
                } else {
                    return;
                }
            }
        }

        // 切换渲染上下文
        mDisplaySurface.makeCurrent();
        mCameraTexture.getTransformMatrix(mMatrix);
        RenderManager.getInstance().setTextureTransformMatirx(mMatrix);
        // 绘制
        draw();
        // 拍照状态
        if (isTakePicture) {
            isTakePicture = false;
            ByteBuffer buffer = mDisplaySurface.getCurrentFrame();
            mCaptureFrameCallback.onFrameCallback(buffer,
                    mDisplaySurface.getWidth(), mDisplaySurface.getHeight());
        }
        mDisplaySurface.swapBuffers();

        // 是否处于录制状态
        if (isRecording && !isRecordingPause) {
            RecordManager.getInstance().frameAvailable();
            int currentTexture = RenderManager.getInstance().getCurrentTexture();
            RecordManager.getInstance()
                    .drawRecorderFrame(currentTexture, mCameraTexture.getTimestamp());
        }
        // 调试信息
        if (isDebug) {
            Log.d(TAG, "drawFrame time = " + (System.currentTimeMillis() - temp));
        }

        // 计算绘制帧
        if (mFrameRateMeter != null) {
            mFrameRateMeter.drawFrameCount();
            if (mWeakFpsHandler != null && mWeakFpsHandler.get() != null) {
                mWeakFpsHandler.get().sendMessage(mWeakFpsHandler.get()
                        .obtainMessage(FrameRateMeter.MSG_GAIN_FPS, mFrameRateMeter.getFPS()));
            }
        }
    }

    /**
     * 绘制图像数据到FBO
     */
    private void draw() {
        // 绘制
        RenderManager.getInstance().drawFrame(mCameraTextureId);


    }


    /**
     * 拍照
     */
    void takePicture() {
        isTakePicture = true;
    }

    /**
     * 设置拍照回调
     * @param callback
     */
    void setCaptureFrameCallback(CaptureFrameCallback callback) {
        mCaptureFrameCallback = callback;
    }



    /**
     * 开始录制
     */
    void startRecording() {
        if (mEglCore != null && isPreviewing) {
            // 设置渲染Texture 的宽高
            RecordManager.getInstance().setTextureSize(mImageWidth, mImageHeight);
            // 设置预览大小
            RecordManager.getInstance().setDisplaySize(mViewWidth, mViewHeight);
            // 这里将EGLContext传递到录制线程共享。
            // 由于EGLContext是当前线程手动创建，也就是OpenGLES的mainThread
            // 这里需要传自己手动创建的EglContext
            RecordManager.getInstance().startRecording(mEglCore.getEGLContext());
        }
        isRecording = true;
    }

    /**
     * 暂停录制
     */
    void pauseRecording() {
        RecordManager.getInstance().pauseRecording();
        isRecordingPause = true;
    }

    /**
     * 继续录制
     */
    void continueRecording() {
        RecordManager.getInstance().continueRecording();
        isRecordingPause = false;
    }

    /**
     * 停止录制
     */
    void stopRecording() {
        RecordManager.getInstance().stopRecording();
        isRecording = false;
    }

    /**
     * 重新打开相机
     */
    synchronized void reopenCamera() {
        if (isRecording) {
            stopRecording();
        }

        if (isPreviewing) {
            isPreviewing = false;
        }
        // 重新打开相机
        mPreviewBuffer = CameraUtils.reopenCamera(mContext, mCameraTexture,
                this, mPreviewBuffer);
        // 调整图片大小
        calculateImageSize();
        // 重新调整输入的TextureSize
        RenderManager.getInstance().onInputSizeChanged(mImageWidth, mImageHeight);
        // 重置人脸检测管理器
        int cameraId = CameraUtils.getCameraID();

        isPreviewing = true;
    }

    /**
     * 切换相机
     */
    synchronized void switchCamera() {
        int cameraId = 1 - CameraUtils.getCameraID();
        mPreviewBuffer = CameraUtils.switchCamera(mContext, cameraId, mCameraTexture,
                this, mPreviewBuffer);

    }

    /**
     * 添加新的一帧
     */
    private void addNewFrame() {
        synchronized (mSyncFrameNum) {
            if (isPreviewing) {
                ++mFrameNum;
                if (mRenderHandler != null) {
                    mRenderHandler.removeMessages(RenderHandler.MSG_FRAME);
                    mRenderHandler.sendMessageAtFrontOfQueue(mRenderHandler
                            .obtainMessage(RenderHandler.MSG_FRAME));
                }
            }
        }
    }

    /**
     * 设置Fps的handler回调
     * @param handler
     */
    void setFpsHandler(Handler handler) {
        mWeakFpsHandler = new WeakReference<Handler>(handler);
        mFrameRateMeter = new FrameRateMeter();
    }

    /**
     * 设置渲染状态回调
     * @param listener
     */
    void setRenderStateChangedListener(RenderStateChangedListener listener) {
        mRenderStateListener = listener;
    }
}
