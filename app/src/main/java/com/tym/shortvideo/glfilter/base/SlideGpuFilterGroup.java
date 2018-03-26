package com.tym.shortvideo.glfilter.base;

import android.opengl.GLES20;
import android.view.MotionEvent;
import android.widget.Scroller;

import com.tym.shortvideo.MyApplication;
import com.tym.shortvideo.camerarender.FilterManager;
import com.tym.shortvideo.type.GLFilterType;
import com.tym.shortvideo.type.GlUtil;


/**
 * Created by cj on 2017/7/20 0020.
 * 滑动切换滤镜的控制类
 */

public class SlideGpuFilterGroup {
    private GLFilterType[] types = new GLFilterType[]{
            GLFilterType.SOURCE,         
            GLFilterType.AMARO,          
            GLFilterType.ANTIQUE,       
            GLFilterType.BLACKCAT,      
            GLFilterType.BLACKWHITE,    
            GLFilterType.BROOKLYN,      
            GLFilterType.CALM,         
            GLFilterType.COOL,          
            GLFilterType.EARLYBIRD,     
            GLFilterType.EMERALD,      
            GLFilterType.EVERGREEN,    
            GLFilterType.FAIRYTALE,      
            GLFilterType.FREUD,          
            GLFilterType.HEALTHY,    
            GLFilterType.HEFE,       
            GLFilterType.HUDSON,       
            GLFilterType.KEVIN,       
            GLFilterType.LATTE,     
            GLFilterType.LOMO,       
            GLFilterType.NOSTALGIA,     
            GLFilterType.ROMANCE,     
            GLFilterType.SAKURA,      
            GLFilterType.SKETCH,       
            GLFilterType.SUNSET,       
            GLFilterType.WHITECAT,      
            GLFilterType.WHITENORREDDEN,
    };
    private GLImageFilter curFilter;
    private GLImageFilter leftFilter;
    private GLImageFilter rightFilter;
    private int width, height;
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];
    private int curIndex = 0;
    private Scroller scroller;
    private OnFilterChangeListener mListener;

    public SlideGpuFilterGroup() {

        scroller = new Scroller(MyApplication.getContext());
    }

    private void initFilter() {
        curFilter = getFilter(getCurIndex());
        leftFilter = getFilter(getLeftIndex());
        rightFilter = getFilter(getRightIndex());
    }

    private GLImageFilter getFilter(int index) {
        return FilterManager.getFilter(types[index]);
    }

    public void init() {
        initFilter();
    }

    public void onSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
        GLES20.glGenFramebuffers(1, fFrame, 0);
        GlUtil.genTexturesWithParameter(1, fTexture, 0, GLES20.GL_RGBA, width, height);
        onFilterSizeChanged(width, height);
    }

    private void onFilterSizeChanged(int width, int height) {
        curFilter.onInputSizeChanged(width, height);
        leftFilter.onInputSizeChanged(width, height);
        rightFilter.onInputSizeChanged(width, height);
        curFilter.onDisplayChanged(width, height);
        leftFilter.onDisplayChanged(width, height);
        rightFilter.onDisplayChanged(width, height);
    }

    public int getOutputTexture() {
        return fTexture[0];
    }

    public void drawFrame(int textureId) {
        GlUtil.bindFrameTexture(fFrame[0], fTexture[0]);
        if (direction == 0 && offset == 0) {
            curFilter.drawFrame(textureId);
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
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(0, 0, offset, height);
        leftFilter.drawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(offset, 0, width - offset, height);
        curFilter.drawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    private void drawSlideRight(int textureId) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(0, 0, width - offset, height);
        curFilter.drawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(width - offset, 0, offset, height);
        rightFilter.drawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    private void reCreateRightFilter() {
        decreaseCurIndex();
        rightFilter.release();
        rightFilter = curFilter;
        curFilter = leftFilter;
        leftFilter = getFilter(getLeftIndex());
        leftFilter.onDisplayChanged(width, height);
        leftFilter.onInputSizeChanged(width, height);
        needSwitch = false;
    }

    private void reCreateLeftFilter() {
        increaseCurIndex();
        leftFilter.release();
        leftFilter = curFilter;
        curFilter = rightFilter;
        rightFilter = getFilter(getRightIndex());
        rightFilter.onDisplayChanged(width, height);
        rightFilter.onInputSizeChanged(width, height);
        needSwitch = false;
    }

    public void release() {
        curFilter.release();
        leftFilter.release();
        rightFilter.release();
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
                if (offset > MyApplication.screenWidth / 3) {
                    scroller.startScroll(offset, 0, MyApplication.screenWidth - offset, 0, 100 * (1 - offset / MyApplication.screenWidth));
                    needSwitch = true;
                } else {
                    scroller.startScroll(offset, 0, -offset, 0, 100 * (offset / MyApplication.screenWidth));
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
        void onFilterChange(GLFilterType type);
    }
}
