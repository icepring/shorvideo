package com.tym.shortvideo.filter.helper;

import android.opengl.GLES30;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.Scroller;

import com.tym.shortvideo.filter.base.GPUImageFilter;
import com.tym.shortvideo.filter.helper.type.GlUtil;
import com.tym.shortvideo.recodrender.ParamsManager;


/**
 * Created by cj on 2017/7/20 0020.
 * 滑动切换滤镜的控制类
 */

public class SlideGpuFilterGroup {
    private final int screenWidth,screenHeight;
    private MagicFilterType[] types = new MagicFilterType[]{
            MagicFilterType.NONE,
            MagicFilterType.AMARO,
            MagicFilterType.WARM,
            MagicFilterType.ANTIQUE,
            MagicFilterType.INKWELL,
            MagicFilterType.BRANNAN,
            MagicFilterType.N1977,
            MagicFilterType.FREUD,
            MagicFilterType.HEFE,
            MagicFilterType.HUDSON,
            MagicFilterType.NASHVILLE,
            MagicFilterType.COOL
    };
    private GPUImageFilter curFilter;
    private GPUImageFilter leftFilter;
    private GPUImageFilter rightFilter;
    private int width, height;
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];
    private int curIndex = 0;
    private Scroller scroller;
    private OnFilterChangeListener mListener;

    public SlideGpuFilterGroup() {
        initFilter();
        DisplayMetrics mDisplayMetrics = ParamsManager.context.getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;
        scroller = new Scroller(ParamsManager.context);
    }

    private void initFilter() {
        curFilter = getFilter(getCurIndex());
        leftFilter = getFilter(getLeftIndex());
        rightFilter = getFilter(getRightIndex());
    }

    private GPUImageFilter getFilter(int index) {
        GPUImageFilter filter = MagicFilterFactory.initFilters(types[index]);
        if (filter == null) {
            filter = new GPUImageFilter();
        }
        return filter;
    }

    public void init() {
        curFilter.init();
        leftFilter.init();
        rightFilter.init();
    }

    public void onSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
        GLES30.glGenFramebuffers(1, fFrame, 0);
        GlUtil.genTexturesWithParameter(1, fTexture, 0, GLES30.GL_RGBA, width, height);
        onFilterSizeChanged(width, height);
    }

    private void onFilterSizeChanged(int width, int height) {
        curFilter.onInputSizeChanged(width, height);
        leftFilter.onInputSizeChanged(width, height);
        rightFilter.onInputSizeChanged(width, height);
        curFilter.onDisplaySizeChanged(width, height);
        leftFilter.onDisplaySizeChanged(width, height);
        rightFilter.onDisplaySizeChanged(width, height);
    }

    public int getOutputTexture() {
        return fTexture[0];
    }

    public void onDrawFrame(int textureId) {
        GlUtil.bindFrameTexture(fFrame[0], fTexture[0]);
        if (direction == 0 && offset == 0) {
            curFilter.onDrawFrame(textureId);
        } else if (direction == 1) {
            onDrawSlideLeft(textureId);
        } else if (direction == -1) {
            onDrawSlideRight(textureId);
        }
        GlUtil.unBindFrameBuffer();
    }

    private void onDrawSlideLeft(int textureId) {
        if (locked && scroller.computeScrollOffset()) {
            offset = scroller.getCurrX();
            drawSlideLeft(textureId);
        } else {
            drawSlideLeft(textureId);
            if (locked) {
                if (needSwitch) {
                    reCreateRightFilter();
                    if (mListener != null) {
                        mListener.onFilterChange(types[curIndex]);
                    }
                }
                offset = 0;
                direction = 0;
                locked = false;
            }
        }
    }

    private void onDrawSlideRight(int textureId) {
        if (locked && scroller.computeScrollOffset()) {
            offset = scroller.getCurrX();
            drawSlideRight(textureId);
        } else {
            drawSlideRight(textureId);
            if (locked) {
                if (needSwitch) {
                    reCreateLeftFilter();
                    if (mListener != null) {
                        mListener.onFilterChange(types[curIndex]);
                    }
                }
                offset = 0;
                direction = 0;
                locked = false;
            }
        }
    }

    private void drawSlideLeft(int textureId) {
        GLES30.glViewport(0, 0, width, height);
        GLES30.glEnable(GLES30.GL_SCISSOR_TEST);
        GLES30.glScissor(0, 0, offset, height);
        leftFilter.onDrawFrame(textureId);
        GLES30.glDisable(GLES30.GL_SCISSOR_TEST);
        GLES30.glViewport(0, 0, width, height);
        GLES30.glEnable(GLES30.GL_SCISSOR_TEST);
        GLES30.glScissor(offset, 0, width - offset, height);
        curFilter.onDrawFrame(textureId);
        GLES30.glDisable(GLES30.GL_SCISSOR_TEST);
    }

    private void drawSlideRight(int textureId) {
        GLES30.glViewport(0, 0, width, height);
        GLES30.glEnable(GLES30.GL_SCISSOR_TEST);
        GLES30.glScissor(0, 0, width - offset, height);
        curFilter.onDrawFrame(textureId);
        GLES30.glDisable(GLES30.GL_SCISSOR_TEST);
        GLES30.glViewport(0, 0, width, height);
        GLES30.glEnable(GLES30.GL_SCISSOR_TEST);
        GLES30.glScissor(width - offset, 0, offset, height);
        rightFilter.onDrawFrame(textureId);
        GLES30.glDisable(GLES30.GL_SCISSOR_TEST);
    }

    private void reCreateRightFilter() {
        decreaseCurIndex();
        rightFilter.destroy();
        rightFilter = curFilter;
        curFilter = leftFilter;
        leftFilter = getFilter(getLeftIndex());
        leftFilter.init();
        leftFilter.onDisplaySizeChanged(width, height);
        leftFilter.onInputSizeChanged(width, height);
        needSwitch = false;
    }

    private void reCreateLeftFilter() {
        increaseCurIndex();
        leftFilter.destroy();
        leftFilter = curFilter;
        curFilter = rightFilter;
        rightFilter = getFilter(getRightIndex());
        rightFilter.init();
        rightFilter.onDisplaySizeChanged(width, height);
        rightFilter.onInputSizeChanged(width, height);
        needSwitch = false;
    }

    public void destroy() {
        curFilter.destroy();
        leftFilter.destroy();
        rightFilter.destroy();
    }

    private int getLeftIndex() {
        int leftIndex = curIndex - 1;
        if (leftIndex < 0) {
            leftIndex = types.length - 1;
        }
        return leftIndex;
    }

    private int getRightIndex() {
        int rightIndex = curIndex + 1;
        if (rightIndex >= types.length) {
            rightIndex = 0;
        }
        return rightIndex;
    }

    private int getCurIndex() {
        return curIndex;
    }

    private void increaseCurIndex() {
        curIndex++;
        if (curIndex >= types.length) {
            curIndex = 0;
        }
    }

    private void decreaseCurIndex() {
        curIndex--;
        if (curIndex < 0) {
            curIndex = types.length - 1;
        }
    }

    int downX;
    int direction;//0为静止,-1为向左滑,1为向右滑
    int offset;
    boolean locked;
    boolean needSwitch;

    public void onTouchEvent(MotionEvent event) {
        if (locked) {
            return;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (downX == -1) {
                    return;
                }
                int curX = (int) event.getX();
                if (curX > downX) {
                    direction = 1;
                } else {
                    direction = -1;
                }
                offset = Math.abs(curX - downX);
                break;
            case MotionEvent.ACTION_UP:
                if (downX == -1) {
                    return;
                }
                if (offset == 0) {
                    return;
                }
                locked = true;
                downX = -1;
                if (offset > screenWidth / 3) {
                    scroller.startScroll(offset, 0, screenWidth - offset, 0, 100 * (1 - offset / screenWidth));
                    needSwitch = true;
                } else {
                    scroller.startScroll(offset, 0, -offset, 0, 100 * (offset / screenWidth));
                    needSwitch = false;
                }
                break;
                default:
        }
    }

    public void setOnFilterChangeListener(OnFilterChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnFilterChangeListener {
        void onFilterChange(MagicFilterType type);
    }
}
