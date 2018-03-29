package com.tym.shortvideo.recodrender;

import com.tym.shortvideo.filter.base.GLDisplayFilter;
import com.tym.shortvideo.filter.base.GPUImageFilter;
import com.tym.shortvideo.filter.base.gpuvideo.GLDefaultFilterGroup;
import com.tym.shortvideo.filter.base.gpuvideo.GLImageFilterGroup;
import com.tym.shortvideo.filter.helper.type.GLFilterGroupType;
import com.tym.shortvideo.filter.helper.type.GLFilterIndex;
import com.tym.shortvideo.filter.helper.type.GLFilterType;

import java.util.HashMap;

/**
 * Filter管理类
 * Created by cain on 17-7-25.
 */

public final class FilterManager {

    private static HashMap<GLFilterType, GLFilterIndex> mIndexMap = new HashMap<>();

    static {
        mIndexMap.put(GLFilterType.NONE, GLFilterIndex.NoneIndex);

        // 图片编辑
        mIndexMap.put(GLFilterType.BRIGHTNESS, GLFilterIndex.ImageEditIndex);
        mIndexMap.put(GLFilterType.CONTRAST, GLFilterIndex.ImageEditIndex);
        mIndexMap.put(GLFilterType.EXPOSURE, GLFilterIndex.ImageEditIndex);
        mIndexMap.put(GLFilterType.GUASS, GLFilterIndex.ImageEditIndex);
        mIndexMap.put(GLFilterType.HUE, GLFilterIndex.ImageEditIndex);
        mIndexMap.put(GLFilterType.MIRROR, GLFilterIndex.ImageEditIndex);
        mIndexMap.put(GLFilterType.SATURATION, GLFilterIndex.ImageEditIndex);
        mIndexMap.put(GLFilterType.SHARPNESS, GLFilterIndex.ImageEditIndex);

        // 水印
        mIndexMap.put(GLFilterType.WATERMASK, GLFilterIndex.WaterMaskIndex);

        // 美颜
        mIndexMap.put(GLFilterType.REALTIMEBEAUTY, GLFilterIndex.BeautyIndex);

        // 瘦脸大眼
        mIndexMap.put(GLFilterType.FACESTRETCH, GLFilterIndex.FaceStretchIndex);

        // 贴纸
        mIndexMap.put(GLFilterType.STICKER, GLFilterIndex.StickerIndex);

        // 彩妆
        mIndexMap.put(GLFilterType.MAKEUP, GLFilterIndex.MakeUpIndex);


        // 颜色滤镜
        mIndexMap.put(GLFilterType.AMARO, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.ANTIQUE, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.BLACKCAT, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.BLACKWHITE, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.BROOKLYN, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.CALM, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.COOL, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.EARLYBIRD, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.EMERALD, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.EVERGREEN, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.FAIRYTALE, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.FREUD, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.HEALTHY, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.HEFE, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.HUDSON, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.KEVIN, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.LATTE, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.LOMO, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.NOSTALGIA, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.ROMANCE, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.SAKURA, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.SKETCH, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.SOURCE, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.SUNSET, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.WHITECAT, GLFilterIndex.ColorIndex);
        mIndexMap.put(GLFilterType.WHITENORREDDEN, GLFilterIndex.ColorIndex);
    }

    private FilterManager() {
    }

    public static GPUImageFilter getFilter(GLFilterType type) {
        switch (type) {

            case NONE:      // 没有滤镜
            case SOURCE:    // 原图
            default:
                return new GLDisplayFilter();
        }
    }

    /**
     * 获取滤镜组
     *
     * @return
     */
    public static GLImageFilterGroup getFilterGroup() {
        return new GLDefaultFilterGroup();
    }

    public static GLImageFilterGroup getFilterGroup(GLFilterGroupType type) {
        switch (type) {

            // 默认滤镜组
            case DEFAULT:
            default:
                return new GLDefaultFilterGroup();
        }
    }

    /**
     * 获取层级
     *
     * @param Type
     * @return
     */
    public static GLFilterIndex getIndex(GLFilterType Type) {
        GLFilterIndex index = mIndexMap.get(Type);
        if (index != null) {
            return index;
        }
        return GLFilterIndex.NoneIndex;
    }
}
