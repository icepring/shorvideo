package com.tym.shortvideo.glfilter.image;

import android.opengl.GLES30;

import com.tym.shortvideo.glfilter.base.GLImageFilter;
/**
 * 对比度
 * Created by cain.huang on 2017/8/8.
 */

public class GLContrastFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER =
            "varying highp vec2 textureCoordinate;                                                              \n" +
            "                                                                                                   \n" +
            "uniform sampler2D inputTexture;                                                                    \n" +
            "uniform lowp float contrast;                                                                       \n" +
            "                                                                                                   \n" +
            "void main()                                                                                        \n" +
            "{                                                                                                  \n" +
            "     lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);                          \n" +
            "                                                                                                   \n" +
            "     gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w); \n" +
            "}                                                                                                  ";


    private int mContrastLoc;
    private float mContrast;

    public GLContrastFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLContrastFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mContrastLoc = GLES30.glGetUniformLocation(mProgramHandle, "contrast");
        setContrast(1.0f);
    }

    /**
     * 设置对比度
     * @param contrast
     */
    public void setContrast(float contrast) {
        mContrast = contrast;
        setFloat(mContrastLoc, mContrast);
    }
}
