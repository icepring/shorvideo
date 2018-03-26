package com.tym.shortvideo.tymtymtym.gpufilter.filter;

import android.opengl.GLES30;

import com.tym.shortvideo.camerarender.ParamsManager;
import com.tym.shortvideo.tymtymtym.gpufilter.basefilter.GPUImageFilter;
import com.tym.shortvideo.type.GlUtil;

/**
 * @Author Jliuer
 * @Date 2018/03/26/17:02
 * @Email Jliuer@aliyun.com
 * @Description
 */
public class MagicAmaroFilter extends GPUImageFilter {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    " \n" +
                    " varying mediump vec2 textureCoordinate;\n" +
                    " \n" +
                    " uniform sampler2D inputTexture;\n" +
                    " uniform sampler2D blowoutTexture; //blowout;\n" +
                    " uniform sampler2D overlayTexture; //overlay;\n" +
                    " uniform sampler2D mapTexture; //map\n" +
                    " \n" +
                    " uniform float strength;\n" +
                    "\n" +
                    " void main()\n" +
                    " {\n" +
                    "     vec4 originColor = texture2D(inputTexture, textureCoordinate);\n" +
                    "     vec4 texel = texture2D(inputTexture, textureCoordinate);\n" +
                    "     vec3 bbTexel = texture2D(blowoutTexture, textureCoordinate).rgb;\n" +
                    "     \n" +
                    "     texel.r = texture2D(overlayTexture, vec2(bbTexel.r, texel.r)).r;\n" +
                    "     texel.g = texture2D(overlayTexture, vec2(bbTexel.g, texel.g)).g;\n" +
                    "     texel.b = texture2D(overlayTexture, vec2(bbTexel.b, texel.b)).b;\n" +
                    "     \n" +
                    "     vec4 mapped;\n" +
                    "     mapped.r = texture2D(mapTexture, vec2(texel.r, 0.16666)).r;\n" +
                    "     mapped.g = texture2D(mapTexture, vec2(texel.g, 0.5)).g;\n" +
                    "     mapped.b = texture2D(mapTexture, vec2(texel.b, 0.83333)).b;\n" +
                    "     mapped.a = 1.0;\n" +
                    "     \n" +
                    "     mapped.rgb = mix(originColor.rgb, mapped.rgb, strength);\n" +
                    "\n" +
                    "     gl_FragColor = mapped;\n" +
                    " }";

    private int mStrength;
    private int mStrengthLoc;

    private int mBlowoutTexture;
    private int mBlowoutTextureLoc;

    private int mOverlayTexture;
    private int mOverlayTextureLoc;

    private int mMapTexture;
    private int mMapTextureLoc;

    public MagicAmaroFilter() {
        super(NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER);
    }

    @Override
    protected void onDestroy() {
        GLES30.glDeleteTextures(3, new int[]{mBlowoutTexture, mOverlayTexture, mMapTexture}, 0);
        super.onDestroy();
    }

    @Override
    protected void onInit() {
        super.onInit();
        mStrengthLoc = GLES30.glGetUniformLocation(mGLProgId, "strength");
        mBlowoutTextureLoc = GLES30.glGetUniformLocation(mGLProgId, "blowoutTexture");
        mOverlayTextureLoc = GLES30.glGetUniformLocation(mGLProgId, "overlayTexture");
        mMapTextureLoc = GLES30.glGetUniformLocation(mGLProgId, "mapTexture");
    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mBlowoutTexture);
        GLES30.glUniform1i(mBlowoutTextureLoc, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(getTextureType(), mOverlayTexture);
        GLES30.glUniform1i(mOverlayTextureLoc, 2);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(getTextureType(), mMapTexture);
        GLES30.glUniform1i(mMapTextureLoc, 3);
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        mBlowoutTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/amaro_blowout.png");
        mMapTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/amaro_map.png");
        mOverlayTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/amaro_overlay.png");
        setFloat(mStrengthLoc, 1.0f);
    }
}
