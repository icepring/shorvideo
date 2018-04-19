package com.tym.shortvideo.filter.advanced;

import android.opengl.GLES30;

import com.tym.video.R;
import com.tym.shortvideo.filter.base.GPUImageFilter;
import com.tym.shortvideo.filter.helper.OpenGlUtils;
import com.tym.shortvideo.filter.helper.type.GlUtil;
import com.tym.shortvideo.recodrender.ParamsManager;
public class MagicN1977Filter extends GPUImageFilter {
    private int[] inputTextureHandles = {-1, -1};
    private int[] inputTextureUniformLocations = {-1, -1};
    private int mGLStrengthLocation;

    public MagicN1977Filter() {
        super(R.raw.n1977);
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
        for (int i = 0; i < inputTextureUniformLocations.length; i++)
            inputTextureUniformLocations[i] = GLES30.glGetUniformLocation(getProgram(), "inputImageTexture" + (2 + i));
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
                inputTextureHandles[0] = GlUtil.createTextureFromAssets(ParamsManager.context, "filter/n1977map.png");
                inputTextureHandles[1] = GlUtil.createTextureFromAssets(ParamsManager.context, "filter/n1977blowout.png");
            }
        });
    }
}
