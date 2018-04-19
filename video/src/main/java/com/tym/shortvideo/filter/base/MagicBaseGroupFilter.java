package com.tym.shortvideo.filter.base;


import android.opengl.GLES30;


import com.tym.shortvideo.filter.helper.OpenGlUtils;

import java.nio.FloatBuffer;
import java.util.List;


public class MagicBaseGroupFilter extends GPUImageFilter {
    protected static int[] frameBuffers = null;
    protected static int[] frameBufferTextures = null;
    private int frameWidth = -1;
    private int frameHeight = -1;
    protected List<GPUImageFilter> filters;

    public MagicBaseGroupFilter(List<GPUImageFilter> filters) {
        super();
        this.filters = filters;
    }

    @Override
    public void onDestroy() {
        for (GPUImageFilter filter : filters) {
            filter.destroy();
        }
        destroyFramebuffers();
    }

    @Override
    public void init() {
        for (GPUImageFilter filter : filters) {
            filter.init();
        }
    }

    @Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);
        int size = filters.size();
        for (int i = 0; i < size; i++) {
            filters.get(i).onInputSizeChanged(width, height);
        }
        if (frameBuffers != null && (frameWidth != width || frameHeight != height || frameBuffers.length != size - 1)) {
            destroyFramebuffers();
            frameWidth = width;
            frameHeight = height;
        }
        if (frameBuffers == null) {
            frameBuffers = new int[size - 1];
            frameBufferTextures = new int[size - 1];

            for (int i = 0; i < size - 1; i++) {
                GLES30.glGenFramebuffers(1, frameBuffers, i);

                GLES30.glGenTextures(1, frameBufferTextures, i);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, frameBufferTextures[i]);
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0,
                        GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffers[i]);
                GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                        GLES30.GL_TEXTURE_2D, frameBufferTextures[i], 0);

                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            }
        }
    }

    @Override
    public int onDrawFrame(final int textureId, final FloatBuffer cubeBuffer,
                           final FloatBuffer textureBuffer) {
        if (frameBuffers == null || frameBufferTextures == null) {
            return OpenGlUtils.NOT_INIT;
        }
        int size = filters.size();
        int previousTexture = textureId;
        for (int i = 0; i < size; i++) {
            GPUImageFilter filter = filters.get(i);
            boolean isNotLast = i < size - 1;
            if (isNotLast) {
                GLES30.glViewport(0, 0, mIntputWidth, mIntputHeight);
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffers[i]);
                GLES30.glClearColor(0, 0, 0, 0);
                filter.onDrawFrame(previousTexture, mGLCubeBuffer, mGLTextureBuffer);
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                previousTexture = frameBufferTextures[i];
            } else {
                GLES30.glViewport(0, 0, mOutputWidth, mOutputHeight);
                filter.onDrawFrame(previousTexture, cubeBuffer, textureBuffer);
            }
        }
        return OpenGlUtils.ON_DRAWN;
    }

    @Override
    public int onDrawFrame(final int textureId) {
        if (frameBuffers == null || frameBufferTextures == null) {
            return OpenGlUtils.NOT_INIT;
        }
        int size = filters.size();
        int previousTexture = textureId;
        for (int i = 0; i < size; i++) {
            GPUImageFilter filter = filters.get(i);
            boolean isNotLast = i < size - 1;
            if (isNotLast) {
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffers[i]);
                GLES30.glClearColor(0, 0, 0, 0);
                filter.onDrawFrame(previousTexture, mGLCubeBuffer, mGLTextureBuffer);
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                previousTexture = frameBufferTextures[i];
            } else {
                filter.onDrawFrame(previousTexture, mGLCubeBuffer, mGLTextureBuffer);
            }
        }
        return OpenGlUtils.ON_DRAWN;
    }

    private void destroyFramebuffers() {
        if (frameBufferTextures != null) {
            GLES30.glDeleteTextures(frameBufferTextures.length, frameBufferTextures, 0);
            frameBufferTextures = null;
        }
        if (frameBuffers != null) {
            GLES30.glDeleteFramebuffers(frameBuffers.length, frameBuffers, 0);
            frameBuffers = null;
        }
    }

    public int getSize() {
        return filters.size();
    }
}
