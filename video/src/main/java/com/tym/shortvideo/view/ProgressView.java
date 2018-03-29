package com.tym.shortvideo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tym.video.R;
import com.tym.shortvideo.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;


public class ProgressView extends View {

    /**
     * 进度条
     */
    private Paint mProgressPaint;

    /**
     * 闪
     */
    private Paint mActivePaint;

    /**
     * 暂停/中断色块
     */
    private Paint mPausePaint;

    /**
     * 回删
     */
    private Paint mRemovePaint;

    /**
     * 三秒
     */
    private Paint mThreePaint;
    /**
     * 超时
     */
    private Paint mOverflowPaint;
    private boolean mStop, mProgressChanged;
    private boolean mActiveState;
    /**
     * 最长时长
     */
    private int mMaxDuration, mVLineWidth;
    private int mRecordTimeMin;

    // 当前进度(默认值为0)
    private float mProgress = 0;
    private float mCurrentLenght = 0;
    private float mStartLenght = 0;

    private boolean mDeleteMode = false;

    // 分割列表
    private List<Float> mSplitList = new ArrayList<>();

    public ProgressView(Context paramContext) {
        super(paramContext);
        init();

    }

    public ProgressView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    public ProgressView(Context paramContext, AttributeSet paramAttributeSet,
                        int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void init() {

        mMaxDuration = 4 * 1000;
        mRecordTimeMin = 1 * 1000;

        mProgressPaint = new Paint();
        mActivePaint = new Paint();
        mPausePaint = new Paint();
        mRemovePaint = new Paint();
        mThreePaint = new Paint();
        mOverflowPaint = new Paint();

        mVLineWidth = DeviceUtils.dipToPX(getContext(), 1);

        setBackgroundColor(getResources().getColor(R.color.camera_bg));
        mProgressPaint.setColor(0xFF45C01A);
        mProgressPaint.setStyle(Paint.Style.FILL);

        mActivePaint.setColor(getResources().getColor(android.R.color.white));
        mActivePaint.setStyle(Paint.Style.FILL);

        mPausePaint.setColor(getResources().getColor(
                R.color.camera_progress_split));
        mPausePaint.setStyle(Paint.Style.FILL);

        mRemovePaint.setColor(getResources().getColor(
                R.color.camera_progress_delete));
        mRemovePaint.setStyle(Paint.Style.FILL);

        mThreePaint.setColor(getResources().getColor(
                R.color.camera_progress_three));
        mThreePaint.setStyle(Paint.Style.FILL);

        mOverflowPaint.setColor(getResources().getColor(
                R.color.camera_progress_overflow));
        mOverflowPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 闪动
     */
    private final static int HANDLER_INVALIDATE_ACTIVE = 0;
    /**
     * 录制中
     */
    private final static int HANDLER_INVALIDATE_RECORDING = 1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_INVALIDATE_ACTIVE:
                    invalidate();
                    mActiveState = !mActiveState;
                    if (!mStop) {
                        sendEmptyMessageDelayed(0, 300);
                    }
                    break;
                case HANDLER_INVALIDATE_RECORDING:
                    invalidate();
                    if (mProgressChanged) {
                        sendEmptyMessageDelayed(0, 50);
                    }
                    break;
                default:
            }

            super.dispatchMessage(msg);
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int width = getMeasuredWidth(), height = getMeasuredHeight();
        int left = 0, right = 0;
        right = left + (int) (width * mCurrentLenght);

        canvas.drawRect(left, 0.0F, width * mCurrentLenght, height,
                mProgressPaint);

        if (isDeleteMode()) {
            if (mSplitList.size() > 0) {
                float split = mSplitList.get(mSplitList.size() - 1);
                canvas.drawRect(width * split, 0.0F, width * mCurrentLenght, height, mRemovePaint);
            }
        }
        // 绘制断点
        for (int i = 0; i < mSplitList.size(); i++) {
            if (i != 0) {
                canvas.drawRect(mSplitList.get(i) * width - mVLineWidth, 0.0F, mSplitList.get(i) * width, height,
                        mPausePaint);
            }
        }

        if (mProgress < mRecordTimeMin) {
            left = (int) ((mRecordTimeMin * 1.0f) / mMaxDuration * width);
            canvas.drawRect(left, 0.0F, left + mVLineWidth, height, mThreePaint);
        }

        if (mActiveState) {
            if (right + 8 >= width) {
                right = width - 8;
            }
            canvas.drawRect(right, 0.0F, right + 8, getMeasuredHeight(),
                    mActivePaint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mStop = false;
        mHandler.sendEmptyMessage(HANDLER_INVALIDATE_ACTIVE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mStop = true;
        mHandler.removeMessages(HANDLER_INVALIDATE_ACTIVE);
    }

    /**
     * 设置当前进度
     */
    public void setProgress(float progress) {
        mProgress = progress;
        mCurrentLenght = progress / mMaxDuration;
        invalidate();
        // 满进度回调
        if (mCurrentLenght >= 1) {

        }
    }

    /**
     * 设置段点
     */
    public void addSplitView() {
        Log.d("addSplitView", "mGrithPro = " + mCurrentLenght);
        mSplitList.add(mCurrentLenght);
        invalidate();
    }

    /**
     * 删除最后一个段点
     */
    public void deleteSplitView() {
        if (mDeleteMode && mSplitList.size() > 0) {
            mSplitList.remove(mSplitList.size() - 1);
            mDeleteMode = false;
            invalidate();
        }
    }

    /**
     * 清除断点
     */
    public void cleanSplitView() {
        if (mSplitList.size() > 0) {
            mSplitList.clear();
            invalidate();
        }
    }

    public List<Float> getSplitList() {
        return mSplitList;
    }

    /**
     * 设置删除模式
     */
    public void setDeleteMode(boolean deleteMode) {
        mDeleteMode = deleteMode;
        invalidate();
    }

    /**
     * 是否处于删除模式
     */
    public boolean isDeleteMode() {
        return mDeleteMode;
    }

    public void setProgressMax(int duration) {
        this.mMaxDuration = duration;
    }

    public void start() {
        mProgressChanged = true;
    }

    public void stop() {
        mProgressChanged = false;
    }

    public void setProgressMin(int recordTimeMin) {
        this.mRecordTimeMin = recordTimeMin;
    }
}
