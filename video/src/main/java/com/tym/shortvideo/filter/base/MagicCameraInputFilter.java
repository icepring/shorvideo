package com.tym.shortvideo.filter.base;


import com.tym.video.R;
import com.tym.shortvideo.filter.helper.OpenGlUtils;

/**
 * 绘制纹理到屏幕
 *
 * @author Created by jz on 2017/5/2 17:53
 */
public class MagicCameraInputFilter extends GPUImageFilter {

    //这里的顶点着色器没有矩阵参数
    public MagicCameraInputFilter() {
        super(OpenGlUtils.readShaderFromRawResource(R.raw.default_vertex), OpenGlUtils.readShaderFromRawResource(R.raw.default_fragment));
    }

}