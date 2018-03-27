package com.tym.shortvideo.glfilter.color;

import android.opengl.GLES20;

import com.tym.shortvideo.MyApplication;
import com.tym.shortvideo.R;
import com.tym.shortvideo.glfilter.base.GPUImageFilter;
import com.tym.shortvideo.glfilter.helper.OpenGlUtils;


public class MagicN1977Filter extends GPUImageFilter {
	private int[] inputTextureHandles = {-1,-1};
	private int[] inputTextureUniformLocations = {-1,-1};
    private int mGLStrengthLocation;

	public MagicN1977Filter(){
		super(NO_FILTER_VERTEX_SHADER, OpenGlUtils.readShaderFromRawResource(R.raw.n1977));
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
		    	inputTextureHandles[0] = OpenGlUtils.loadTexture(MyApplication.getContext(), "filter/n1977map.png");
				inputTextureHandles[1] = OpenGlUtils.loadTexture(MyApplication.getContext(), "filter/n1977blowout.png");
		    }
	    });
	}
}
