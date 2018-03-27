package com.tym.shortvideo.glfilter.color;

import android.opengl.GLES20;

import com.tym.shortvideo.MyApplication;
import com.tym.shortvideo.R;
import com.tym.shortvideo.filter.base.GPUImageFilter;
import com.tym.shortvideo.filter.helper.OpenGlUtils;

/**
 * @Author Jliuer
 * @Date 2018/3/27/22:53
 * @Email Jliuer@aliyun.com
 * @Description 哈德森
 */
public class MagicHudsonFilter extends GPUImageFilter {
	private int[] inputTextureHandles = {-1,-1,-1};
	private int[] inputTextureUniformLocations = {-1,-1,-1};
    private int mGLStrengthLocation;

	public MagicHudsonFilter(){
		super(NO_FILTER_VERTEX_SHADER, OpenGlUtils.readShaderFromRawResource(R.raw.hudson));
	}
	
	@Override
	protected void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(inputTextureHandles.length, inputTextureHandles, 0);
        for(int i = 0; i < inputTextureHandles.length; i++) {
			inputTextureHandles[i] = -1;
		}
    }
	
	@Override
	protected void onDrawArraysAfter(){
		for(int i = 0; i < inputTextureHandles.length
				&& inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++){
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3));
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		}
	}
	  
	@Override
	protected void onDrawArraysPre(){
		for(int i = 0; i < inputTextureHandles.length 
				&& inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++){
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3) );
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureHandles[i]);
			GLES20.glUniform1i(inputTextureUniformLocations[i], (i+3));
		}
	}
	
	@Override
	protected void onInit(){
		super.onInit();
		for(int i=0; i < inputTextureUniformLocations.length; i++) {
			inputTextureUniformLocations[i] = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture" + (2 + i));
		}
		mGLStrengthLocation = GLES20.glGetUniformLocation(mGLProgId,
				"strength");
	}
	
	@Override
	protected void onInitialized(){
		super.onInitialized();
		setFloat(mGLStrengthLocation, 1.0f);
	    runOnDraw(new Runnable(){
		    @Override
			public void run(){
		    	inputTextureHandles[0] = OpenGlUtils.loadTexture(MyApplication.getContext(), "filter/hudsonbackground.png");
				inputTextureHandles[1] = OpenGlUtils.loadTexture(MyApplication.getContext(), "filter/overlaymap.png");
				inputTextureHandles[2] = OpenGlUtils.loadTexture(MyApplication.getContext(), "filter/hudsonmap.png");
		    }
	    });
	}
}
