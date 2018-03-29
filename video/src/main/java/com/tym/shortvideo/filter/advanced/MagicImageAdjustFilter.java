package com.tym.shortvideo.filter.advanced;


import com.tym.shortvideo.filter.base.MagicBaseGroupFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageBrightnessFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageContrastFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageSaturationFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageSharpenFilter;
import com.tym.shortvideo.filter.base.GPUImageFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageExposureFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageHueFilter;

import java.util.ArrayList;
import java.util.List;

public class MagicImageAdjustFilter extends MagicBaseGroupFilter {
	
	public MagicImageAdjustFilter() {
		super(initFilters());
	}
	
	private static List<GPUImageFilter> initFilters(){
		List<GPUImageFilter> filters = new ArrayList<>();
		filters.add(new GPUImageContrastFilter());
		filters.add(new GPUImageBrightnessFilter());
		filters.add(new GPUImageExposureFilter());
		filters.add(new GPUImageHueFilter());
		filters.add(new GPUImageSaturationFilter());
		filters.add(new GPUImageSharpenFilter());
		return filters;		
	}
	
	public void setSharpness(final float range){
		((GPUImageSharpenFilter) filters.get(5)).setSharpness(range);
	}
	
	public void setHue(final float range){
		((GPUImageHueFilter) filters.get(3)).setHue(range);
	}
	
	public void setBrightness(final float range){
		((GPUImageBrightnessFilter) filters.get(1)).setBrightness(range);
	}
	
	public void setContrast(final float range){
		((GPUImageContrastFilter) filters.get(0)).setContrast(range);
	}
	
	public void setSaturation(final float range){
		((GPUImageSaturationFilter) filters.get(4)).setSaturation(range);
	}
	
	public void setExposure(final float range){
		((GPUImageExposureFilter) filters.get(2)).setExposure(range);
	}
}
