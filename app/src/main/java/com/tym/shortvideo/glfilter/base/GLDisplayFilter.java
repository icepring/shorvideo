package com.tym.shortvideo.glfilter.base;

import com.tym.shortvideo.tymtymtym.gpufilter.basefilter.GPUImageFilter;

/**
 * 预览的滤镜
 * Created by cain.huang on 2017/9/29.
 */
public class GLDisplayFilter extends GPUImageFilter {

    public GLDisplayFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public GLDisplayFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }
}
