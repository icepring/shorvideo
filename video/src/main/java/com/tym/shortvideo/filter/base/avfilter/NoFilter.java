package com.tym.shortvideo.filter.base.avfilter;

import android.content.res.Resources;
import android.opengl.GLES30;


public class NoFilter extends AFilter {

    public NoFilter(Resources res) {
        super(res);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.sh",
            "shader/base_fragment.sh");
    }

    /**
     * 背景默认为黑色
     */
    @Override
    protected void onClear() {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
