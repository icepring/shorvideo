package com.tym.shortvideo.glfilter.advanced;

import android.opengl.GLES30;

import com.tym.shortvideo.tymtymtym.gpufilter.basefilter.GPUImageFilter;
import com.tym.shortvideo.type.GlUtil;
import com.tym.shortvideo.type.TextureRotationUtils;

import java.nio.FloatBuffer;
import java.util.LinkedList;

/**
 * @Author Jliuer
 * @Date 2018/03/27/17:38
 * @Email Jliuer@aliyun.com
 * @Description
 */
public class GLDisplayFilter extends GPUImageFilter {
    protected static final String NO_FILTER_VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;                                   \n" +
                    "attribute vec4 aPosition;                                  \n" +
                    "attribute vec4 aTextureCoord;                              \n" +
                    "varying vec2 textureCoordinate;                            \n" +
                    "void main() {                                              \n" +
                    "    gl_Position = uMVPMatrix * aPosition;                  \n" +
                    "    textureCoordinate = aTextureCoord.xy;                  \n" +
                    "}                                                          \n";

    protected static final String NO_FILTER_FRAGMENT_SHADER =
            "precision mediump float;                                   \n" +
                    "varying vec2 textureCoordinate;                            \n" +
                    "uniform sampler2D inputTexture;                                \n" +
                    "void main() {                                              \n" +
                    "    gl_FragColor = texture2D(inputTexture, textureCoordinate); \n" +
                    "}                                                          \n";

    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtil.createFloatBuffer(TextureRotationUtils.CubeVertices);

    protected FloatBuffer mVertexArray = FULL_RECTANGLE_BUF;
    protected FloatBuffer mTexCoordArray = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices);
    protected int mCoordsPerVertex = TextureRotationUtils.CoordsPerVertex;
    protected int mVertexCount = TextureRotationUtils.CubeVertices.length / mCoordsPerVertex;

    protected int mProgramHandle;
    protected int muMVPMatrixLoc;
    protected int maPositionLoc;
    protected int maTextureCoordLoc;
    protected int mInputTextureLoc;

    // 渲染的Image的宽高
    protected int mImageWidth;
    protected int mImageHeight;
    // 显示输出的宽高
    protected int mDisplayWidth;
    protected int mDisplayHeight;

    // 变换矩阵
    protected float[] mMVPMatrix = new float[16];
    // 缩放矩阵
    protected float[] mTexMatrix = new float[16];

    private final LinkedList<Runnable> mRunOnDraw;

    public GLDisplayFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public GLDisplayFilter(String vertexShader, String fragmentShader) {
        mRunOnDraw = new LinkedList<>();
        mProgramHandle = GlUtil.createProgram(vertexShader, fragmentShader);
        initHandle();
        initIdentityMatrix();
    }

    /**
     * 初始化句柄
     */
    protected void initHandle() {
        maPositionLoc = GLES30.glGetAttribLocation(mProgramHandle, "aPosition");
        maTextureCoordLoc = GLES30.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        muMVPMatrixLoc = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        mInputTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "inputTexture");
    }

    /**
     * Surface发生变化时调用
     *
     * @param width
     * @param height
     */
    @Override
    public void onInputSizeChanged(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        mDisplayWidth = width;
        mDisplayHeight = height;
    }

    /**
     * 绘制Frame
     *
     * @param textureId
     */
    @Override
    public boolean drawFrame(int textureId) {
        return drawFrame(textureId, mVertexArray, mTexCoordArray);
    }

    /**
     * 绘制Frame
     *
     * @param textureId
     * @param vertexBuffer
     * @param textureBuffer
     */
    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer,
                             FloatBuffer textureBuffer) {
        GLES30.glUseProgram(mProgramHandle);
        runPendingOnDrawTasks();
        // 绑定数据
        bindValue(textureId, vertexBuffer, textureBuffer);
        onDrawArraysPre();
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mVertexCount);
        onDrawArraysAfter();
        unBindValue();
        GLES30.glUseProgram(0);
        return true;
    }

    /**
     * 绑定数据
     *
     * @param textureId
     * @param vertexBuffer
     * @param textureBuffer
     */
    protected void bindValue(int textureId, FloatBuffer vertexBuffer,
                             FloatBuffer textureBuffer) {
        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES30.GL_FLOAT, false, 0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(maPositionLoc);

        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES30.GL_FLOAT, false, 0, textureBuffer);
        GLES30.glEnableVertexAttribArray(maTextureCoordLoc);

        GLES30.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(getTextureType(), textureId);
        GLES30.glUniform1i(mInputTextureLoc, 0);
    }

    /**
     * 解除绑定
     */
    protected void unBindValue() {
        GLES30.glDisableVertexAttribArray(maPositionLoc);
        GLES30.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES30.glBindTexture(getTextureType(), 0);
    }

    /**
     * 获取Texture类型
     * GLES30.TEXTURE_2D / GLES11Ext.GL_TEXTURE_EXTERNAL_OES等
     */
    @Override
    public int getTextureType() {
        return GLES30.GL_TEXTURE_2D;
    }

    /**
     * 释放资源
     */
    public void release() {
        GLES30.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }
}
