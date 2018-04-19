package com.tym.shortvideo.filter.base.avfilter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.tym.shortvideo.utils.MatrixUtils;


/**
 * @author Jliuer
 * @Date 18/03/28 10:14
 * @Email Jliuer@aliyun.com
 * @Description 水印的Filter
 */
public class WaterMarkFilter extends NoFilter{
    /**水印的放置位置和宽高*/
    private int x,y,w,h;
    /**控件的大小*/
    private int width,height;
    /**水印图片的bitmap*/
    private Bitmap mBitmap;
    /***/
    private NoFilter mFilter;

    public WaterMarkFilter(Resources mRes) {
        super(mRes);
        mFilter=new NoFilter(mRes){
            @Override
            protected void onClear() {
            }
        };
    }
    public void setWaterMark(Bitmap bitmap){
        if(this.mBitmap!=null){
            this.mBitmap.recycle();
        }
        this.mBitmap=bitmap;
    }
    @Override
    public void draw() {
        super.draw();
        GLES30.glViewport(x,y,w == 0 ? mBitmap.getWidth():w,h==0?mBitmap.getHeight():h);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_COLOR, GLES30.GL_DST_ALPHA);
        mFilter.draw();
        GLES30.glDisable(GLES30.GL_BLEND);
        GLES30.glViewport(0,0,width,height);
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mFilter.create();
        createTexture();
    }
    private int[] textures=new int[1];
    private void createTexture() {
        if(mBitmap!=null){
            //生成纹理
            GLES30.glGenTextures(1,textures,0);
            //生成纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textures[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mBitmap, 0);
            //对画面进行矩阵旋转
            MatrixUtils.flip(mFilter.getMatrix(),false,true);

            mFilter.setTextureId(textures[0]);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        this.width=width;
        this.height=height;
        mFilter.setSize(width,height);
    }
    public void setPosition(int x,int y,int width,int height){
        this.x=x;
        this.y=y;
        this.w=width;
        this.h=height;
    }
}
