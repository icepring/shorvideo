package com.tym.shortvideo.glfilter.helper;


import com.tym.shortvideo.glfilter.base.GPUImageFilter;
import com.tym.shortvideo.glfilter.color.MagicAmaroFilter;
import com.tym.shortvideo.glfilter.color.MagicAntiqueFilter;
import com.tym.shortvideo.glfilter.color.MagicBrannanFilter;
import com.tym.shortvideo.glfilter.color.MagicCoolFilter;
import com.tym.shortvideo.glfilter.color.MagicFreudFilter;
import com.tym.shortvideo.glfilter.color.MagicHefeFilter;
import com.tym.shortvideo.glfilter.color.MagicHudsonFilter;
import com.tym.shortvideo.glfilter.color.MagicInkwellFilter;
import com.tym.shortvideo.glfilter.color.MagicN1977Filter;
import com.tym.shortvideo.glfilter.color.MagicNashvilleFilter;

public class MagicFilterFactory {

    private static MagicFilterType filterType = MagicFilterType.NONE;

    public static GPUImageFilter initFilters(MagicFilterType type) {
        if (type == null) {
            return null;
        }
        filterType = type;
        switch (type) {
            case ANTIQUE:
                return new MagicAntiqueFilter();
            case BRANNAN:
                return new MagicBrannanFilter();
            case FREUD:
                return new MagicFreudFilter();
            case HEFE:
                return new MagicHefeFilter();
            case HUDSON:
                return new MagicHudsonFilter();
            case INKWELL:
                return new MagicInkwellFilter();
            case N1977:
                return new MagicN1977Filter();
            case NASHVILLE:
                return new MagicNashvilleFilter();
            case COOL:
                return new MagicCoolFilter();
            case WARM:
                return new MagicWarmFilter();
            case AMARO:
                return new MagicAmaroFilter();
            default:
                return null;
        }
    }

    public MagicFilterType getCurrentFilterType() {
        return filterType;
    }

    private static class MagicWarmFilter extends GPUImageFilter {
    }
}
