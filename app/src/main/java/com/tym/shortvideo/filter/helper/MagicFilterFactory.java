package com.tym.shortvideo.filter.helper;

import com.tym.shortvideo.filter.advanced.MagicAmaroFilter;
import com.tym.shortvideo.filter.advanced.MagicAntiqueFilter;
import com.tym.shortvideo.filter.advanced.MagicBlackCatFilter;
import com.tym.shortvideo.filter.advanced.MagicBrannanFilter;
import com.tym.shortvideo.filter.advanced.MagicBrooklynFilter;
import com.tym.shortvideo.filter.advanced.MagicCalmFilter;
import com.tym.shortvideo.filter.advanced.MagicCoolFilter;
import com.tym.shortvideo.filter.advanced.MagicCrayonFilter;
import com.tym.shortvideo.filter.advanced.MagicEarlyBirdFilter;
import com.tym.shortvideo.filter.advanced.MagicEmeraldFilter;
import com.tym.shortvideo.filter.advanced.MagicEvergreenFilter;
import com.tym.shortvideo.filter.advanced.MagicFairytaleFilter;
import com.tym.shortvideo.filter.advanced.MagicFreudFilter;
import com.tym.shortvideo.filter.advanced.MagicHealthyFilter;
import com.tym.shortvideo.filter.advanced.MagicHefeFilter;
import com.tym.shortvideo.filter.advanced.MagicHudsonFilter;
import com.tym.shortvideo.filter.advanced.MagicImageAdjustFilter;
import com.tym.shortvideo.filter.advanced.MagicInkwellFilter;
import com.tym.shortvideo.filter.advanced.MagicKevinFilter;
import com.tym.shortvideo.filter.advanced.MagicLatteFilter;
import com.tym.shortvideo.filter.advanced.MagicLomoFilter;
import com.tym.shortvideo.filter.advanced.MagicN1977Filter;
import com.tym.shortvideo.filter.advanced.MagicNashvilleFilter;
import com.tym.shortvideo.filter.advanced.MagicNostalgiaFilter;
import com.tym.shortvideo.filter.advanced.MagicPixarFilter;
import com.tym.shortvideo.filter.advanced.MagicRiseFilter;
import com.tym.shortvideo.filter.advanced.MagicRomanceFilter;
import com.tym.shortvideo.filter.advanced.MagicSakuraFilter;
import com.tym.shortvideo.filter.advanced.MagicSierraFilter;
import com.tym.shortvideo.filter.advanced.MagicSketchFilter;
import com.tym.shortvideo.filter.advanced.MagicSkinWhitenFilter;
import com.tym.shortvideo.filter.advanced.MagicSunriseFilter;
import com.tym.shortvideo.filter.advanced.MagicSunsetFilter;
import com.tym.shortvideo.filter.advanced.MagicSutroFilter;
import com.tym.shortvideo.filter.advanced.MagicSweetsFilter;
import com.tym.shortvideo.filter.advanced.MagicTenderFilter;
import com.tym.shortvideo.filter.advanced.MagicToasterFilter;
import com.tym.shortvideo.filter.advanced.MagicValenciaFilter;
import com.tym.shortvideo.filter.advanced.MagicWaldenFilter;
import com.tym.shortvideo.filter.advanced.MagicWarmFilter;
import com.tym.shortvideo.filter.advanced.MagicWhiteCatFilter;
import com.tym.shortvideo.filter.advanced.MagicWhiteOrReddenFilter;
import com.tym.shortvideo.filter.advanced.MagicXproIIFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageBrightnessFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageContrastFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageExposureFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageHueFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageSaturationFilter;
import com.tym.shortvideo.filter.base.gpuimage.GPUImageSharpenFilter;
import com.tym.shortvideo.filter.base.GPUImageFilter;
import com.tym.shortvideo.filter.advanced.MagicBeautyFilter;

/**
 * 滤镜简单工厂
 *
 * @author Created by jz on 2017/5/2 16:56
 */
public class MagicFilterFactory {

    public static GPUImageFilter initFilters(MagicFilterType type) {
        if (type == null) {
            return null;
        }
        filterType = type;
        switch (type) {
            case NONE:
                return new GPUImageFilter();
            case WHITECAT:
                return new MagicWhiteCatFilter();
            case BLACKCAT:
                return new MagicBlackCatFilter();
            case BEAUTY:
                return new MagicBeautyFilter();
            case SKINWHITEN:
                return new MagicSkinWhitenFilter();
            case ROMANCE:
                return new MagicRomanceFilter();
            case SAKURA:
                return new MagicSakuraFilter();
            case AMARO:
                return new MagicAmaroFilter();
            case WALDEN:
                return new MagicWaldenFilter();
            case ANTIQUE:
                return new MagicAntiqueFilter();
            case CALM:
                return new MagicCalmFilter();
            case BRANNAN:
                return new MagicBrannanFilter();
            case BROOKLYN:
                return new MagicBrooklynFilter();
            case EARLYBIRD:
                return new MagicEarlyBirdFilter();
            case FREUD:
                return new MagicFreudFilter();
            case HEFE:
                return new MagicHefeFilter();
            case HUDSON:
                return new MagicHudsonFilter();
            case INKWELL:
                return new MagicInkwellFilter();
            case KEVIN:
                return new MagicKevinFilter();
            case LOMO:
                return new MagicLomoFilter();
            case N1977:
                return new MagicN1977Filter();
            case NASHVILLE:
                return new MagicNashvilleFilter();
            case PIXAR:
                return new MagicPixarFilter();
            case RISE:
                return new MagicRiseFilter();
            case SIERRA:
                return new MagicSierraFilter();
            case SUTRO:
                return new MagicSutroFilter();
            case TOASTER2:
                return new MagicToasterFilter();
            case VALENCIA:
                return new MagicValenciaFilter();
            case XPROII:
                return new MagicXproIIFilter();
            case EVERGREEN:
                return new MagicEvergreenFilter();
            case HEALTHY:
                return new MagicHealthyFilter();
            case COOL:
                return new MagicCoolFilter();
            case EMERALD:
                return new MagicEmeraldFilter();
            case LATTE:
                return new MagicLatteFilter();
            case WARM:
                return new MagicWarmFilter();
            case TENDER:
                return new MagicTenderFilter();
            case SWEETS:
                return new MagicSweetsFilter();
            case NOSTALGIA:
                return new MagicNostalgiaFilter();
            case FAIRYTALE:
                return new MagicFairytaleFilter();
            case SUNRISE:
                return new MagicSunriseFilter();
            case SUNSET:
                return new MagicSunsetFilter();
            case CRAYON:
                return new MagicCrayonFilter();
            case SKETCH:
                return new MagicSketchFilter();
            case WHITENORREDDEN:
                return new MagicWhiteOrReddenFilter();
            //image adjust
            case BRIGHTNESS:
                return new GPUImageBrightnessFilter();
            case CONTRAST:
                return new GPUImageContrastFilter();
            case EXPOSURE:
                return new GPUImageExposureFilter();
            case HUE:
                return new GPUImageHueFilter();
            case SATURATION:
                return new GPUImageSaturationFilter();
            case SHARPEN:
                return new GPUImageSharpenFilter();
            case IMAGE_ADJUST:
                return new MagicImageAdjustFilter();
            default:
                return null;
        }
    }

    public MagicFilterType getCurrentFilterType() {
        return filterType;
    }

    private static MagicFilterType filterType = MagicFilterType.NONE;
}
