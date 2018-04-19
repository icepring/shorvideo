package com.tym.shortvideo.filter.base.avfilter;

import android.content.res.Resources;
import android.opengl.GLES30;

import com.tym.shortvideo.utils.MatrixUtils;
import com.tym.shortvideo.filter.helper.type.GlUtil;

/**
 * @author Jliuer
 * @Date 18/03/28 10:14
 * @Email Jliuer@aliyun.com
 * @Description  draw并不执行父类的draw方法,所以矩阵对它无效
 */
public class ProcessFilter extends AFilter {

    private AFilter mFilter;
    //创建离屏buffer
    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[1];

    private int width;
    private int height;


    public ProcessFilter(Resources mRes) {
        super(mRes);
        mFilter=new NoFilter(mRes);
        float[]  OM= MatrixUtils.getOriginalMatrix();
        MatrixUtils.flip(OM,false,true);//矩阵上下翻转
        mFilter.setMatrix(OM);
    }

    @Override
    protected void initBuffer() {

    }

    @Override
    protected void onCreate() {
        mFilter.create();
    }

    @Override
    public int getOutputTexture() {
        return fTexture[0];
    }

    @Override
    public void draw() {
        boolean b= GLES30.glIsEnabled(GLES30.GL_CULL_FACE);
        if(b){
            GLES30.glDisable(GLES30.GL_CULL_FACE);
        }
        GLES30.glViewport(0,0,width,height);
        GlUtil.bindFrameTexture(fFrame[0],fTexture[0]);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT,
            GLES30.GL_RENDERBUFFER, fRender[0]);
        mFilter.setTextureId(getTextureId());
        mFilter.draw();
        GlUtil.unBindFrameBuffer();
        if(b){
            GLES30.glEnable(GLES30.GL_CULL_FACE);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        if(this.width!=width&&this.height!=height){
            this.width=width;
            this.height=height;
            mFilter.setSize(width,height);
            deleteFrameBuffer();
            GLES30.glGenFramebuffers(1,fFrame,0);
            GLES30.glGenRenderbuffers(1,fRender,0);
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER,fRender[0]);
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16,
                width, height);
            GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT,
                GLES30.GL_RENDERBUFFER, fRender[0]);
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER,0);
            GlUtil.genTexturesWithParameter(1,fTexture,0, GLES30.GL_RGBA,width,height);
        }
    }

    private void deleteFrameBuffer() {
        GLES30.glDeleteRenderbuffers(1, fRender, 0);
        GLES30.glDeleteFramebuffers(1, fFrame, 0);
        GLES30.glDeleteTextures(1, fTexture, 0);
    }

}
