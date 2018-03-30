package com.tym.shortvideo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tym.shortvideo.recodrender.CaptureFrameCallback;
import com.tym.shortvideo.recodrender.ColorFilterManager;
import com.tym.shortvideo.recodrender.DrawerManager;
import com.tym.shortvideo.recodrender.FrameRateMeter;
import com.tym.shortvideo.recodrender.ParamsManager;
import com.tym.shortvideo.recodrender.RecordManager;
import com.tym.shortvideo.recodrender.RenderStateChangedListener;
import com.tym.shortvideo.recordcore.CountDownManager;
import com.tym.shortvideo.recordcore.VideoListManager;
import com.tym.shortvideo.recordcore.multimedia.MediaEncoder;
import com.tym.shortvideo.recordcore.multimedia.VideoCombineManager;
import com.tym.shortvideo.recordcore.multimedia.VideoCombiner;
import com.tym.shortvideo.utils.BitmapUtils;
import com.tym.shortvideo.utils.CameraUtils;
import com.tym.shortvideo.utils.FileUtils;
import com.tym.shortvideo.utils.PermissionUtils;
import com.tym.shortvideo.utils.StringUtils;
import com.tym.shortvideo.filter.helper.type.AspectRatioType;
import com.tym.shortvideo.filter.helper.type.GLType;
import com.tym.shortvideo.filter.helper.type.TextureRotationUtils;
import com.tym.shortvideo.view.AspectFrameLayout;
import com.tym.shortvideo.view.AsyncRecyclerview;
import com.tym.shortvideo.view.CainSurfaceView;
import com.tym.shortvideo.view.ProgressView;
import com.tym.shortvideo.view.ShutterButton;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback, CainSurfaceView.OnClickListener, CainSurfaceView.OnTouchScroller, RenderStateChangedListener,
        SeekBar.OnSeekBarChangeListener, CaptureFrameCallback, ShutterButton.GestureListener {

    private static final String TAG = "CameraActivity";
    private static final boolean VERBOSE = true;
    private static final int REQUEST_PREVIEW = 0x200;

    private static final int MSG_SEND_FPS_HANDLE = 0x010;

    private static final int REQUEST_CAMERA = 0x01;
    private static final int REQUEST_STORAGE = 0x02;
    private static final int REQUEST_RECORD = 0x03;
    private static final int REQUEST_LOCATION = 0x04;

    // 对焦大小
    private static final int FocusSize = 100;
    // 权限使能标志
    private boolean mCameraEnable = false;
    private boolean mStorageWriteEnable = false;
    private boolean mRecordSoundEnable = false;
    private boolean mLocationEnable = false;

    // 状态标志
    private boolean mOnPreviewing = false;
    private boolean mOnRecording = false;

    // 是否显示Fps
    private boolean mShowFps = true;
    private Handler mFpsHandler;

    // 预览部分
    private AspectFrameLayout mAspectLayout;
    private CainSurfaceView mCameraSurfaceView;
    // fps显示

    private TextView mFpsView;
    private Button mBtnLocal;
    private Button mBtnSwitch;
    private Button mBtnLvjing;
    private Button mBtnBeauty;

    // 倒计时
    private TextView mCountDownView;

    private ShutterButton mBtnShutter;
    private ProgressView mProgressView;

    private Button mBtnRecordDelete;
    private Button mBtnRecordPreview;

    // 显示滤镜
    private boolean isShowingEffect = false;
    private AsyncRecyclerview mEffectListView;
    private LinearLayoutManager mEffectManager;
    // 是否需要滚动
    private boolean mEffectNeedToMove = false;

    // 显示贴纸
    private boolean isShowingStickers = false;

    // 当前长宽比类型，默认16:9
    private AspectRatioType mCurrentRatioType = AspectRatioType.Ratio_16_9;

    // 当前长宽比值
    private float mCurrentRatio;

    private int mColorIndex = 0;

    private boolean isDebug = true;
    // 主线程Handler
    private Handler mMainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 持有当前上下文
        String phoneName = Build.MODEL;
        if (phoneName.toLowerCase().contains("bullhead")
                || phoneName.toLowerCase().contains("nexus 5x")) {
            TextureRotationUtils.setBackReverse(true);
            ParamsManager.mBackReverse = true;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        // 创建渲染线程
        DrawerManager.getInstance().createRenderThread(this);
        // 添加渲染状态回调监听
        DrawerManager.getInstance().addRenderStateChangedListener(this);
        // 设置拍照回调
        DrawerManager.getInstance().setCaptureFrameCallback(this);
        mMainHandler = new Handler(getMainLooper());

        mCameraEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.CAMERA);
        mStorageWriteEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mRecordSoundEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.RECORD_AUDIO);
        if (mCameraEnable && mStorageWriteEnable) {
            initView();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA);
        }
    }

    @SuppressLint("HandlerLeak")
    private void initView() {
        mCurrentRatio = CameraUtils.getCurrentRatio();
        mAspectLayout = findViewById(R.id.layout_aspect);

        mAspectLayout.setAspectRatio(mCurrentRatio);
        mCameraSurfaceView = new CainSurfaceView(this);
        mCameraSurfaceView.getHolder().addCallback(this);
//        mCameraSurfaceView.addScroller(this);
        mCameraSurfaceView.addClickListener(this);
        mAspectLayout.addView(mCameraSurfaceView);
        mAspectLayout.requestLayout();

        // 显示fps
        if (mShowFps) {
            mFpsView = findViewById(R.id.tv_fps);
            mFpsHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case FrameRateMeter.MSG_GAIN_FPS:
                            mFpsView.setText("fps = " + (float) msg.obj);
                            break;

                        case MSG_SEND_FPS_HANDLE:
                            if (!DrawerManager.getInstance().hasSetFpsHandle()) {
                                DrawerManager.getInstance().setFpsHandler((Handler) msg.obj);
                                sendMessageDelayed(mFpsHandler.obtainMessage(MSG_SEND_FPS_HANDLE, msg.obj),
                                        1000);
                            } else {
                                removeMessages(MSG_SEND_FPS_HANDLE);
                            }
                            break;
                        default:
                    }
                }
            };
        }
        mBtnLocal = findViewById(R.id.btn_local);
        mBtnLocal.setOnClickListener(this);

        mBtnSwitch = findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);
        mBtnLvjing = findViewById(R.id.btn_lvjing);
        mBtnLvjing.setOnClickListener(this);
        mBtnLvjing.setVisibility(View.GONE);
        mBtnBeauty = findViewById(R.id.btn_beauty);
        mBtnBeauty.setOnClickListener(this);

        mCountDownView = findViewById(R.id.tv_countdown);

        mBtnShutter = findViewById(R.id.btn_take);
        mProgressView = findViewById(R.id.tym_test);
        mBtnShutter.setGestureListener(this);
        mBtnShutter.setOnClickListener(this);

        mBtnRecordDelete = findViewById(R.id.btn_record_delete);
        mBtnRecordDelete.setOnClickListener(this);
        mBtnRecordPreview = findViewById(R.id.btn_record_done);
        mBtnRecordPreview.setOnClickListener(this);

        adjustBottomView();

//        initEffectListView();
        DrawerManager.getInstance().setBeautifyLevel(0);
        onIndicatorChanged(2);
    }

    /**
     * 调整底部视图
     */
    private void adjustBottomView() {
        if (CameraUtils.getCurrentRatio() < CameraUtils.Ratio_4_3) {
            mBtnRecordDelete.setBackgroundResource(R.mipmap.preview_video_delete_white);
            mBtnRecordPreview.setBackgroundResource(R.mipmap.preview_video_done_white);
        } else {
            mBtnRecordDelete.setBackgroundResource(R.mipmap.preview_video_delete_black);
            mBtnRecordPreview.setBackgroundResource(R.mipmap.preview_video_done_black);
        }
    }

    /**
     * 初始化滤镜显示
     */
    private void initEffectListView() {
        // 初始化滤镜图片
        mEffectListView = findViewById(R.id.effect_list);
        mEffectListView.setVisibility(View.GONE);
        mEffectManager = new LinearLayoutManager(this);
        mEffectManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mEffectListView.setLayoutManager(mEffectManager);

        EffectFilterAdapter adapter = new EffectFilterAdapter(this,
                ColorFilterManager.getInstance().getFilterType(),
                ColorFilterManager.getInstance().getFilterName());

        mEffectListView.setAdapter(adapter);
        adapter.addItemClickListener(new EffectFilterAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(int position) {
                mColorIndex = position;
                DrawerManager.getInstance().changeFilterType(
                        ColorFilterManager.getInstance().getColorFilterType(position));
                if (isDebug) {
                    Log.d("changeFilter", "index = " + mColorIndex + ", filter name = "
                            + ColorFilterManager.getInstance().getColorFilterName(mColorIndex));
                }
            }
        });
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    private void requestStorageWritePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
    }

    private void requestRecordPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                },
                REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // 相机权限
            case REQUEST_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraEnable = true;
                    initView();
                }
                break;

            // 存储权限
            case REQUEST_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mStorageWriteEnable = true;
                }
                break;

            // 录音权限
            case REQUEST_RECORD:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mRecordSoundEnable = true;
                }
                break;

            // 位置权限
            case REQUEST_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationEnable = true;
                }
                break;
            default:
        }
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onResume() {
        super.onResume();
        registerHomeReceiver();
        if (mCameraEnable) {
            DrawerManager.getInstance().startPreview();
        } else {
            requestCameraPermission();
        }
        // 判断是否允许写入权限
        if (PermissionUtils.permissionChecking(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            mStorageWriteEnable = true;
        }
        if (isShowingEffect) {
            scrollToCurrentEffect();
        }
        // 是否需要显示Fps
        if (mShowFps) {
            if (mFpsHandler == null) {
                mFpsHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case FrameRateMeter.MSG_GAIN_FPS:
                                mFpsView.setText("fps = " + (float) msg.obj);
                                break;

                            case MSG_SEND_FPS_HANDLE:
                                if (!DrawerManager.getInstance().hasSetFpsHandle()) {
                                    DrawerManager.getInstance().setFpsHandler((Handler) msg.obj);
                                    sendMessageDelayed(mFpsHandler.obtainMessage(MSG_SEND_FPS_HANDLE, msg.obj),
                                            1000);
                                } else {
                                    removeMessages(MSG_SEND_FPS_HANDLE);
                                }
                                break;
                            default:
                        }
                    }
                };
            }
            mFpsHandler.sendMessageDelayed(mFpsHandler
                    .obtainMessage(MSG_SEND_FPS_HANDLE, mFpsHandler), 1000);
        }
    }

    @Override
    public void onBackPressed() {
        if (isShowingEffect) {
            isShowingEffect = false;
            if (mEffectListView != null) {
                mEffectListView.setVisibility(View.GONE);
            }
            return;
        }
        if (isShowingStickers) {
            isShowingStickers = false;
            // TODO 隐藏贴纸选项

            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterHomeReceiver();
        if (mCameraEnable) {
            DrawerManager.getInstance().stopPreview();
        }
    }

    @Override
    protected void onDestroy() {
        DrawerManager.getInstance().destoryTrhead();
        // 在停止时需要释放上下文，防止内存泄漏
        if (mFpsHandler != null) {
            mFpsHandler.removeCallbacksAndMessages(null);
            mFpsHandler = null;
        }
        super.onDestroy();
    }

    private void registerHomeReceiver() {
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomePressReceiver, homeFilter);
    }

    private void unRegisterHomeReceiver() {
        unregisterReceiver(mHomePressReceiver);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        DrawerManager.getInstance().surfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        DrawerManager.getInstance().surfacrChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        DrawerManager.getInstance().surfaceDestroyed();
    }

    /**
     * 监听点击home键
     */
    private BroadcastReceiver mHomePressReceiver = new BroadcastReceiver() {
        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (TextUtils.isEmpty(reason)) {
                    return;
                }
                // 当点击了home键时需要停止预览，防止后台一直持有相机
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    // 停止录制
                    if (mOnRecording) {
                        // 停止录制
                        RecordManager.getInstance().stopRecording();
                        // 取消倒计时
                        CountDownManager.getInstance().cancelTimerWithoutSaving();
                        // 重置进入条
                        mBtnShutter.setProgress((int) CountDownManager.getInstance().getVisibleDuration());
                        mProgressView.setProgress((int) CountDownManager.getInstance().getVisibleDuration());
                        // 删除分割线
                        mBtnShutter.deleteSplitView();
                        mProgressView.deleteSplitView();
                        // 关闭按钮
                        mBtnShutter.closeButton();
                        // 更新时间
                        mCountDownView.setText(CountDownManager.getInstance().getVisibleDurationString());
                    }
                    if (mOnPreviewing) {
                        DrawerManager.getInstance().stopPreview();
                    }
                }
            }
        }
    };

    @Override
    public void onPreviewing(boolean previewing) {
        mOnPreviewing = previewing;
        mBtnShutter.setEnableOpenned(mOnPreviewing);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        // 首先关闭设置页面
        switch (v.getId()) {

            // 切换相机
            case R.id.btn_switch:
                switchCamera();
                break;

            case R.id.btn_local:
                startActivity(new Intent(this, VideoSelectActivity.class));
                break;

            // 显示滤镜
            case R.id.btn_lvjing:
                showFilters();
                break;

            // 拍照或录制
            case R.id.btn_take:
                takePicture();
                break;

            // 删除
            case R.id.btn_record_delete:
                deleteRecordedVideo(false);
                break;

            case R.id.btn_beauty:
                DrawerManager.getInstance().setBeautifyLevel(100);
                break;

            // 完成录制，进入预览
            case R.id.btn_record_done:
                previewRecordVideo();
                break;
            default:
        }
    }

    @Override
    public void swipeBack() {
        mColorIndex++;
        if (mColorIndex >= ColorFilterManager.getInstance().getColorFilterCount()) {
            mColorIndex = 0;
        }
        DrawerManager.getInstance()
                .changeFilterType(ColorFilterManager.getInstance().getColorFilterType(mColorIndex));
        scrollToCurrentEffect();
        if (isDebug) {
            Log.d("changeFilter", "index = " + mColorIndex + ", filter name = "
                    + ColorFilterManager.getInstance().getColorFilterName(mColorIndex));
        }
    }

    @Override
    public void swipeFrontal() {
        mColorIndex--;
        if (mColorIndex < 0) {
            int count = ColorFilterManager.getInstance().getColorFilterCount();
            mColorIndex = count > 0 ? count - 1 : 0;
        }
        DrawerManager.getInstance()
                .changeFilterType(ColorFilterManager.getInstance().getColorFilterType(mColorIndex));

        scrollToCurrentEffect();

        if (isDebug) {
            Log.d("changeFilter", "index = " + mColorIndex + ", filter name = "
                    + ColorFilterManager.getInstance().getColorFilterName(mColorIndex));
        }
    }

    /**
     * 滚动到当前位置
     */
    private void scrollToCurrentEffect() {
        if (isShowingEffect) {
            Log.d("scrollToCurrentEffect", "hahaha");
            int firstItem = mEffectManager.findFirstVisibleItemPosition();
            int lastItem = mEffectManager.findLastVisibleItemPosition();
            if (mColorIndex <= firstItem) {
                mEffectListView.scrollToPosition(mColorIndex);
            } else if (mColorIndex <= lastItem) {
                int top = mEffectListView.getChildAt(mColorIndex - firstItem).getTop();
                mEffectListView.scrollBy(0, top);
            } else {
                mEffectListView.scrollToPosition(mColorIndex);
                mEffectNeedToMove = true;
            }
        }
    }

    @Override
    public void swipeUpper(boolean startInLeft) {
        Log.d(TAG, "swipeUpper, startInLeft ? " + startInLeft);
    }

    @Override
    public void swipeDown(boolean startInLeft) {
        Log.d(TAG, "swipeDown, startInLeft ? " + startInLeft);
    }

    @Override
    public void onClick(float x, float y) {
        surfaceViewClick(x, y);
    }

    @Override
    public void doubleClick(float x, float y) {
    }

    /**
     * 点击SurfaceView
     *
     * @param x x轴坐标
     * @param y y轴坐标
     */
    private void surfaceViewClick(float x, float y) {
        if (isShowingEffect) {
            isShowingEffect = false;
            mEffectListView.setVisibility(View.GONE);
        }
        // 设置聚焦区域
        DrawerManager.getInstance().setFocusAres(CameraUtils.getFocusArea((int) x, (int) y,
                mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight(), FocusSize));
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (!mOnPreviewing) {
            return;
        }
        if (mStorageWriteEnable
                || PermissionUtils.permissionChecking(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (ParamsManager.sMGLType == GLType.PICTURE) {
                DrawerManager.getInstance().takePicture();
            }
        } else {
            requestStorageWritePermission();
        }
    }

    @Override
    public void onFrameCallback(final ByteBuffer buffer, final int width, final int height) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                String filePath = ParamsManager.ImagePath + "CainCamera_"
                        + System.currentTimeMillis() + ".jpeg";
                File file = new File(filePath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    bitmap = BitmapUtils.rotateBitmap(bitmap, 180, true);
                    bitmap = BitmapUtils.flipBitmap(bitmap, true);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bitmap.recycle();
                    bitmap = null;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException e) {
                            // do nothing
                        }
                    }
                }

//                Intent intent=new Intent(CameraActivity.this,PreviewActivity.class);
//                intent.putExtra("path",filePath);
//                startActivity(intent);


//                Intent intent = new Intent(CameraActivity.this,
//                        CapturePreviewActivity.class);
//                // 图片类型
//                intent.putExtra(CapturePreviewActivity.MIMETYPE,
//                        CapturePreviewActivity.TYPE_PICTURE);
//                intent.putExtra(CapturePreviewActivity.PATH, filePath);
//                startActivity(intent);
            }
        });
    }

    @Override
    public void onStartRecord() {
        // 初始化录制线程
        RecordManager.getInstance().initThread();
        // 设置输出路径
        String path = ParamsManager.VideoPath
                + "CainCamera_" + System.currentTimeMillis() + ".mp4";
        RecordManager.getInstance().setOutputPath(path);
        // 是否允许录音，只有录制视频才有音频
        RecordManager.getInstance().setEnableAudioRecording(
                mRecordSoundEnable && ParamsManager.canRecordingAudio
                        && ParamsManager.sMGLType == GLType.VIDEO);
        // 是否允许高清录制
        RecordManager.getInstance().enableHighDefinition(true);
        // 初始化录制器
        RecordManager.getInstance().initRecorder(RecordManager.RECORD_WIDTH,
                RecordManager.RECORD_HEIGHT, mEncoderListener);

        // 隐藏删除按钮
        if (ParamsManager.sMGLType == GLType.VIDEO) {
            mBtnRecordPreview.setVisibility(View.GONE);
            mBtnRecordDelete.setVisibility(View.GONE);
        }
        // 初始化倒计时
        CountDownManager.getInstance().initCountDownTimer();
        CountDownManager.getInstance().setCountDownListener(mCountDownListener);
        mBtnShutter.setProgressMax((int) CountDownManager.getInstance().getMaxMilliSeconds());
        mProgressView.setProgressMax((int) CountDownManager.getInstance().getMaxMilliSeconds());
        mProgressView.setProgressMin((int) CountDownManager.getInstance().getMinMilliSeconds());
        // 添加分割线
        mBtnShutter.addSplitView();
        mProgressView.setDeleteMode(false);
        mProgressView.addSplitView();
        mBtnLocal.setVisibility(View.GONE);
    }

    @Override
    public void onStopRecord() {
        // 停止录制
        DrawerManager.getInstance().stopRecording();
        // 停止倒计时
        CountDownManager.getInstance().stopTimer();
    }

    @Override
    public void onProgressOver() {
        // 如果最后一秒内点击停止录制，则仅仅关闭录制按钮，因为前面已经停止过了，不做跳转
        // 如果最后一秒内没有停止录制，否则停止录制并跳转至预览页面
        if (CountDownManager.getInstance().isLastSecondStop()) {
            // 关闭录制按钮
            mBtnShutter.closeButton();
        } else {
            previewRecordVideo();
        }
    }

    private CountDownManager.CountDownListener
            mCountDownListener = new CountDownManager.CountDownListener() {
        @Override
        public void onProgressChanged(long duration) {
            // 设置进度
            mBtnShutter.setProgress(duration);
            mProgressView.setProgress(duration);
            // 设置时间
            mCountDownView.setText(StringUtils.generateMillisTime((int) duration));
        }
    };

    /**
     * 录制监听器
     */
    private MediaEncoder.MediaEncoderListener
            mEncoderListener = new MediaEncoder.MediaEncoderListener() {

        @Override
        public void onPrepared(MediaEncoder encoder) {
            mPreparedCount++;
            // 没有录音权限、不允许音频录制、允许录制音频并且准备好两个MediaEncoder，就可以开始录制了
            if (!mRecordSoundEnable || !ParamsManager.canRecordingAudio
                    || (ParamsManager.canRecordingAudio && mPreparedCount == 2)
                    || ParamsManager.sMGLType == GLType.GIF) { // 录制GIF，没有音频
                // 准备完成，开始录制
                DrawerManager.getInstance().startRecording();

                // 重置
                mPreparedCount = 0;
            }
        }

        @Override
        public void onStarted(MediaEncoder encoder) {
            mStartedCount++;
            // 没有录音权限、不允许音频录制、允许录制音频并且开始了两个MediaEncoder，就处于录制状态了
            if (!mRecordSoundEnable || !ParamsManager.canRecordingAudio
                    || (ParamsManager.canRecordingAudio && mStartedCount == 2)
                    || ParamsManager.sMGLType == GLType.GIF) { // 录制GIF，没有音频
                mOnRecording = true;

                // 重置状态
                mStartedCount = 0;

                // 编码器已经进入录制状态，则快门按钮可用
                mBtnShutter.setEnableEncoder(true);

                // 开始倒计时
                CountDownManager.getInstance().startTimer();
            }
        }

        @Override
        public void onStopped(MediaEncoder encoder) {
        }

        @Override
        public void onReleased(MediaEncoder encoder) { // 复用器释放完成
            mReleaseCount++;
            // 没有录音权限、不允许音频录制、允许录制音频并且释放了两个MediaEncoder，就完全释放掉了
            if (!mRecordSoundEnable || !ParamsManager.canRecordingAudio
                    || (ParamsManager.canRecordingAudio && mReleaseCount == 2)
                    || ParamsManager.sMGLType == GLType.GIF) { // 录制GIF，没有音频
                // 录制完成跳转预览页面
                String outputPath = RecordManager.getInstance().getOutputPath();
                // 添加分段视频，存在时长为0的情况，也就是取消倒计时但不保存时长的情况
                if (CountDownManager.getInstance().getRealDuration() > 0) {
                    VideoListManager.getInstance().addSubVideo(outputPath,
                            (int) CountDownManager.getInstance().getRealDuration());
                } else {    // 移除多余的视频
                    FileUtils.deleteFile(outputPath);
                }
                // 重置当前走过的时长
                CountDownManager.getInstance().resetDuration();

                // 处于非录制状态
                mOnRecording = false;

                // 显示删除按钮
                if (ParamsManager.sMGLType == GLType.VIDEO) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBtnRecordPreview.setVisibility(View.VISIBLE);
                            mBtnRecordDelete.setVisibility(View.VISIBLE);
                        }
                    });
                }

                // 处于录制状态点击了预览按钮，则需要等待完成再跳转， 或者是处于录制GIF状态
                if (mNeedToWaitStop || ParamsManager.sMGLType == GLType.GIF) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 重置按钮状态
                            // 开始预览
                            previewRecordVideo();
                        }
                    });
                }
                // 重置释放状态
                mReleaseCount = 0;

                // 编码器已经完全释放，则快门按钮可用
                mBtnShutter.setEnableEncoder(true);

            }

        }
    };

    // MediaEncoder准备好的数量
    private int mPreparedCount = 0;

    // 开始MediaEncoder的数量
    private int mStartedCount = 0;

    // 释放MediaEncoder的数量
    private int mReleaseCount = 0;

    /**
     * 删除录制的视频
     */
    synchronized private void deleteRecordedVideo(boolean clearAll) {
        // 处于删除模式，则删除文件
        if (mBtnShutter.isDeleteMode()) {

            // 删除视频，判断是否清除所有
            if (clearAll) {
                // 清除所有分割线
                mBtnShutter.cleanSplitView();
                mProgressView.cleanSplitView();
                VideoListManager.getInstance().removeAllSubVideo();
            } else {
                // 删除分割线
                mBtnShutter.deleteSplitView();
                mProgressView.deleteSplitView();
                VideoListManager.getInstance().removeLastSubVideo();
            }
            // 重置计时器记录走过的时长
            CountDownManager.getInstance().resetDuration();
            // 重置最后一秒点击标志
            CountDownManager.getInstance().resetLastSecondStop();
            // 更新进度
            mBtnShutter.setProgress(CountDownManager.getInstance().getVisibleDuration());
            mProgressView.setProgress(CountDownManager.getInstance().getVisibleDuration());
            // 更新时间
            mCountDownView.setText(CountDownManager.getInstance().getVisibleDurationString());
            // 如果此时没有了视频，则隐藏删除按钮
            if (VideoListManager.getInstance().getSubVideoList().size() <= 0) {
                mBtnRecordDelete.setVisibility(View.GONE);
                mBtnRecordPreview.setVisibility(View.GONE);
                // 复位状态
                mNeedToWaitStop = false;
                mOnRecording = false;
            }

            if (mProgressView.getSplitList().size() == 0) {
                mBtnLocal.setVisibility(View.VISIBLE);
            }

        } else { // 没有进入删除模式则进入删除模式
            mBtnShutter.setDeleteMode(true);
            mProgressView.setDeleteMode(true);
        }
    }

    // 是否需要等待录制完成再跳转
    private boolean mNeedToWaitStop = false;

    /**
     * 等待录制完成再预览录制视频
     */
    private void previewRecordVideo() {
        if (mOnRecording) {
            mNeedToWaitStop = true;
            // 停止录制
            DrawerManager.getInstance().stopRecording();
        } else {
            // 销毁录制线程
            RecordManager.getInstance().destoryThread();
            mNeedToWaitStop = false;
//            combineVideo();
            // 隐藏删除和预览按钮
            mBtnRecordPreview.setVisibility(View.GONE);
            mBtnRecordDelete.setVisibility(View.GONE);


            Intent intent = new Intent(CameraActivity.this,
                    PreviewActivity.class);
            ArrayList<String> arrayList=new ArrayList<>(VideoListManager.getInstance()
                    .getSubVideoPathList());
            intent.putStringArrayListExtra("path", arrayList);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PREVIEW) {
            // 时间清0
            mCountDownView.setText(StringUtils.generateMillisTime(0));
            // 复位进度条
            mBtnShutter.setProgress(0);
            mProgressView.setProgress(0);
            // 清除录制按钮分割线
            mBtnShutter.cleanSplitView();
            mProgressView.cleanSplitView();
            // 关闭录制按钮
            mBtnShutter.closeButton();
        }
    }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (!mCameraEnable) {
            requestCameraPermission();
            return;
        }
        if (mCameraSurfaceView != null) {
            DrawerManager.getInstance().switchCamera();
        }
    }

    /**
     * 显示滤镜
     */
    private void showFilters() {
        isShowingEffect = true;
        if (mEffectListView != null) {
            mEffectListView.setVisibility(View.VISIBLE);
            scrollToCurrentEffect();
        }
    }

    public void onIndicatorChanged(int currentIndex) {
        if (currentIndex == 0) {
            ParamsManager.sMGLType = GLType.GIF;
            // TODO GIF录制后面再做处理
            mBtnShutter.setIsRecorder(true);
        } else if (currentIndex == 1) {
            ParamsManager.sMGLType = GLType.PICTURE;
            // 拍照状态
            mBtnShutter.setIsRecorder(false);
        } else if (currentIndex == 2) {
            ParamsManager.sMGLType = GLType.VIDEO;
            // 录制视频状态
            mBtnShutter.setIsRecorder(true);
            // 请求录音权限
            if (!mRecordSoundEnable) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD);
            }
        }

        // 显示时间
        if (currentIndex == 2) {
            mCountDownView.setVisibility(View.VISIBLE);
        } else {
            mCountDownView.setVisibility(View.GONE);
        }
    }





}
