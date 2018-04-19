package com.tym.shortvideo.filter.advanced;

import android.opengl.GLES30;

import com.tym.shortvideo.filter.base.GPUImageFilter;
import com.tym.shortvideo.filter.helper.OpenGlUtils;
import com.tym.shortvideo.filter.helper.type.GlUtil;
import com.tym.shortvideo.recodrender.ParamsManager;
import com.tym.video.R;


public class MagicAmaroFilter extends GPUImageFilter {
    private int[] inputTextureHandles = {-1, -1, -1};
    private int[] inputTextureUniformLocations = {-1, -1, -1};
    private int mGLStrengthLocation;

    public MagicAmaroFilter() {
        super(R.raw.amaro);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GLES30.glDeleteTextures(inputTextureHandles.length, inputTextureHandles, 0);
        for (int i = 0; i < inputTextureHandles.length; i++) {
            inputTextureHandles[i] = -1;
        }
    }

    @Override
    protected void onDrawArraysAfter() {
        for (int i = 0; i < inputTextureHandles.length
                && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + (i + 3));
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        }
    }

    @Override
    protected void onDrawArraysPre() {
        for (int i = 0; i < inputTextureHandles.length
                && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + (i + 3));
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, inputTextureHandles[i]);
            GLES30.glUniform1i(inputTextureUniformLocations[i], (i + 3));
        }
    }

    @Override
    protected void onInit() {
        super.onInit();
        for (int i = 0; i < inputTextureUniformLocations.length; i++) {
            inputTextureUniformLocations[i] = GLES30.glGetUniformLocation(getProgram(), "inputImageTexture" + (2 + i));
        }
        mGLStrengthLocation = GLES30.glGetUniformLocation(mGLProgId,
                "strength");
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        setFloat(mGLStrengthLocation, 1.0f);
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                inputTextureHandles[0] = GlUtil.createTextureFromAssets(ParamsManager.context, "filter/brannan_blowout.png");
                inputTextureHandles[1] = GlUtil.createTextureFromAssets(ParamsManager.context, "filter/overlaymap.png");
                inputTextureHandles[2] = GlUtil.createTextureFromAssets(ParamsManager.context, "filter/amaromap.png");
            }
        });
    }
}
