package com.tym.shortvideo.filter.advanced;

import android.opengl.GLES30;

import com.tym.shortvideo.filter.base.GPUImageFilter;

/**
 * @Author Jliuer
 * @Date 2018/03/28
 * @Email Jliuer@aliyun.com
 * @Description
 */
public class MagicWhiteOrReddenFilter extends GPUImageFilter {
    private static final String NO_FILTER_FRAGMENT_SHADER =
            "precision highp float;\n" +
                    "varying mediump vec2 textureCoordinate;\n" +
                    "uniform sampler2D inputTexture;\n" +
                    "uniform float redden;\n" +
                    "uniform float whitening;\n" +
                    "uniform float pinking;\n" +
                    "void main () {\n" +
                    "\n" +
                    "    lowp vec4 fragColor = vec4(texture2D (inputTexture, textureCoordinate).xyz, 1.0);\n" +
                    "\n" +
                    "    if ((whitening != 0.0)) {\n" +
                    "        fragColor.xyz = clamp (mix (fragColor.xyz, (vec3(1.0, 1.0, 1.0) -\n" +
                    "        ((vec3(1.0, 1.0, 1.0) - fragColor.xyz) * (vec3(1.0, 1.0, 1.0) - fragColor.xyz))),\n" +
                    "        (whitening * dot (vec3(0.299, 0.587, 0.114), fragColor.xyz))), 0.0, 1.0);\n" +
                    "    };\n" +
                    "\n" +
                    "    if ((redden != 0.0)) {\n" +
                    "        lowp vec3 redColor = mix (fragColor.xyz, (vec3(1.0, 1.0, 1.0) -\n" +
                    "            ((vec3(1.0, 1.0, 1.0) - fragColor.xyz) * (vec3(1.0, 1.0, 1.0) - fragColor.xyz))),\n" +
                    "        (0.2 * redden));\n" +
                    "\n" +
                    "        lowp vec3 tmpvar_3 = mix (vec3(dot (redColor, vec3(0.299, 0.587, 0.114))),\n" +
                    "            redColor, (1.0 + redden));\n" +
                    "        lowp vec3 tmpvar_4 = mix (tmpvar_3.xyy, tmpvar_3, 0.5);\n" +
                    "        lowp float tmpvar_5 = dot (tmpvar_4, vec3(0.299, 0.587, 0.114));\n" +
                    "\n" +
                    "        fragColor.xyz = clamp (mix (tmpvar_3, mix (tmpvar_4, sqrt(tmpvar_4), tmpvar_5),\n" +
                    "                (redden * tmpvar_5)), 0.0, 1.0);\n" +
                    "    };\n" +
                    "\n" +
                    "    if ((pinking != 0.0)) {\n" +
                    "        lowp vec3 pinkColor;\n" +
                    "        pinkColor.x = ((sqrt(fragColor.x) * 0.41) + (0.59 * fragColor.x));\n" +
                    "        pinkColor.y = ((sqrt(fragColor.y) * 0.568) + (0.432 * fragColor.y));\n" +
                    "        pinkColor.z = ((sqrt(fragColor.z) * 0.7640001) + (0.2359999 * fragColor.z));\n" +
                    "        fragColor.xyz = clamp (mix (fragColor.xyz, pinkColor,\n" +
                    "            (pinking * dot (vec3(0.299, 0.587, 0.114), fragColor.xyz))), 0.0, 1.0);\n" +
                    "    };\n" +
                    "    gl_FragColor = fragColor;\n" +
                    "}";

    private int mReddenLoc;
    private int mWhitenLoc;
    private int mPinkingLoc;

    public MagicWhiteOrReddenFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public MagicWhiteOrReddenFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    public void init() {
        super.init();
        mReddenLoc = GLES30.glGetUniformLocation(mGLProgId, "redden");
        mWhitenLoc = GLES30.glGetUniformLocation(mGLProgId, "whitening");
        mPinkingLoc = GLES30.glGetUniformLocation(mGLProgId, "pinking");
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        setReddenValue(1.0f);
        setWhitenValue(1.0f);
        setPinkingValue(1.0f);
    }

    /**
     * 设置红色值
     * @param reddenValue
     */
    public void setReddenValue(float reddenValue) {
        setFloat(mReddenLoc, reddenValue);
    }

    /**
     * 设置白色值
     * @param whitenValue
     */
    public void setWhitenValue(float whitenValue) {
        setFloat(mWhitenLoc, whitenValue);
    }

    /**
     * 设置粉色值
     * @param pinkingValue
     */
    public void setPinkingValue(float pinkingValue) {
        setFloat(mPinkingLoc, pinkingValue);
    }
}
