package com.tym.shortvideo.filter.base;

import android.opengl.GLES30;

import com.tym.video.R;
import com.tym.shortvideo.filter.helper.OpenGlUtils;
import com.tym.shortvideo.filter.helper.type.GlUtil;
import com.tym.shortvideo.recodrender.ParamsManager;

public class MagicLookupFilter extends GPUImageFilter {

    protected String table;

    public MagicLookupFilter(String table) {
        super(R.raw.lookup);
        this.table = table;
    }

    public int mLookupTextureUniform;
    public int mLookupSourceTexture = OpenGlUtils.NO_TEXTURE;

    @Override
    protected void onInit() {
        super.onInit();
        mLookupTextureUniform = GLES30.glGetUniformLocation(getProgram(), "inputImageTexture2");
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                mLookupSourceTexture = GlUtil.createTextureFromAssets(ParamsManager.context, table);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int[] texture = new int[]{mLookupSourceTexture};
        GLES30.glDeleteTextures(1, texture, 0);
        mLookupSourceTexture = -1;
    }

    @Override
    protected void onDrawArraysAfter() {
        if (mLookupSourceTexture != -1) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        }
    }

    @Override
    protected void onDrawArraysPre() {
        if (mLookupSourceTexture != -1) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mLookupSourceTexture);
            GLES30.glUniform1i(mLookupTextureUniform, 3);
        }
    }
}
