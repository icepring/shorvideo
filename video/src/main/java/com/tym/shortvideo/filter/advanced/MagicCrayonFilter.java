package com.tym.shortvideo.filter.advanced;

import android.opengl.GLES20;

import com.tym.video.R;
import com.tym.shortvideo.filter.base.GPUImageFilter;
public class MagicCrayonFilter extends GPUImageFilter {
	
	private int mSingleStepOffsetLocation;
	//1.0 - 5.0
	private int mStrengthLocation;
	
	public MagicCrayonFilter(){
		super(R.raw.crayon);
	}
	
	@Override
    protected void onInit() {
        super.onInit();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
        mStrengthLocation = GLES20.glGetUniformLocation(getProgram(), "strength");
        setFloat(mStrengthLocation, 2.0f);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onInitialized(){
        super.onInitialized();
        setFloat(mStrengthLocation, 0.5f);
    }

    private void setTexelSize(final float w, final float h) {
		setFloatVec2(mSingleStepOffsetLocation, new float[] {1.0f / w, 1.0f / h});
	}
	
	@Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }
}
