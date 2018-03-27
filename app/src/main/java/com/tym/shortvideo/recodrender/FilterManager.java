package com.tym.shortvideo.recodrender;

import com.tym.shortvideo.filter.advanced.MagicBeautyFilter;
import com.tym.shortvideo.glfilter.advanced.GLSketchFilter;
import com.tym.shortvideo.filter.base.GLDisplayFilter;
import com.tym.shortvideo.filter.base.gpuvideo.GLImageFilterGroup;
import com.tym.shortvideo.glfilter.color.GLAmaroFilter;
import com.tym.shortvideo.glfilter.color.GLAnitqueFilter;
import com.tym.shortvideo.glfilter.color.GLBlackCatFilter;
import com.tym.shortvideo.glfilter.color.GLBlackWhiteFilter;
import com.tym.shortvideo.glfilter.color.GLBrooklynFilter;
import com.tym.shortvideo.glfilter.color.GLCalmFilter;
import com.tym.shortvideo.glfilter.color.GLCoolFilter;
import com.tym.shortvideo.glfilter.color.GLEarlyBirdFilter;
import com.tym.shortvideo.glfilter.color.GLEmeraldFilter;
import com.tym.shortvideo.glfilter.color.GLEvergreenFilter;
import com.tym.shortvideo.glfilter.color.GLFairyTaleFilter;
import com.tym.shortvideo.glfilter.color.GLFreudFilter;
import com.tym.shortvideo.glfilter.color.GLHealthyFilter;
import com.tym.shortvideo.glfilter.color.GLHefeFilter;
import com.tym.shortvideo.glfilter.color.GLHudsonFilter;
import com.tym.shortvideo.glfilter.color.GLKevinFilter;
import com.tym.shortvideo.glfilter.color.GLLatteFilter;
import com.tym.shortvideo.glfilter.color.GLLomoFilter;
import com.tym.shortvideo.glfilter.color.GLNostalgiaFilter;
import com.tym.shortvideo.glfilter.color.GLRomanceFilter;
import com.tym.shortvideo.glfilter.color.GLSakuraFilter;
import com.tym.shortvideo.glfilter.color.GLSunsetFilter;
import com.tym.shortvideo.glfilter.color.GLWhiteCatFilter;
import com.tym.shortvideo.glfilter.color.MagicSkinWhitenFilter;
import com.tym.shortvideo.filter.base.gpuvideo.GLDefaultFilterGroup;
import com.tym.shortvideo.glfilter.group.GLMakeUpFilterGroup;
import com.tym.shortvideo.glfilter.image.GLBrightnessFilter;
import com.tym.shortvideo.glfilter.image.GLContrastFilter;
import com.tym.shortvideo.glfilter.image.GLExposureFilter;
import com.tym.shortvideo.glfilter.image.GLGuassFilter;
import com.tym.shortvideo.glfilter.image.GLHueFilter;
import com.tym.shortvideo.glfilter.image.GLMirrorFilter;
import com.tym.shortvideo.glfilter.image.GLSaturationFilter;
import com.tym.shortvideo.glfilter.image.GLSharpnessFilter;
import com.tym.shortvideo.glfilter.sticker.GLStickerFilter;
import com.tym.shortvideo.filter.base.GPUImageFilter;
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

            // 图片基本属性编辑滤镜
            // 饱和度
            case SATURATION:
                return new GLSaturationFilter();
            // 镜像翻转
            case MIRROR:
                return new GLMirrorFilter();
            // 高斯模糊
            case GUASS:
                return new GLGuassFilter();
            // 亮度
            case BRIGHTNESS:
                return new GLBrightnessFilter();
            // 对比度
            case CONTRAST:
                return new GLContrastFilter();
            // 曝光
            case EXPOSURE:
                return new GLExposureFilter();
            // 色调
            case HUE:
                return new GLHueFilter();
            // 锐度
            case SHARPNESS:
                return new GLSharpnessFilter();

            // TODO 贴纸滤镜需要人脸关键点计算得到
            case STICKER:
//                return new DisplayFilter();
                return new GLStickerFilter();

            // 白皙还是红润
            case WHITENORREDDEN:
//                return new MagicAmaroFilter();
                return new MagicSkinWhitenFilter();
//                return new WhitenOrReddenFilter();
            // 实时磨皮
            case REALTIMEBEAUTY:
//                return new GLRealtimeBeautyFilter();
                return new MagicBeautyFilter();

            // AMARO
            case AMARO:
                return new GLAmaroFilter();
            // 古董
            case ANTIQUE:
                return new GLAnitqueFilter();

            // 黑猫
            case BLACKCAT:
                return new GLBlackCatFilter();

            // 黑白
            case BLACKWHITE:
                return new GLBlackWhiteFilter();

            // 布鲁克林
            case BROOKLYN:
                return new GLBrooklynFilter();

            // 冷静
            case CALM:
                return new GLCalmFilter();

            // 冷色调
            case COOL:
                return new GLCoolFilter();

            // 晨鸟
            case EARLYBIRD:
                return new GLEarlyBirdFilter();

            // 翡翠
            case EMERALD:
                return new GLEmeraldFilter();

            // 常绿
            case EVERGREEN:
                return new GLEvergreenFilter();

            // 童话
            case FAIRYTALE:
                return new GLFairyTaleFilter();

            // 佛洛伊特
            case FREUD:
                return new GLFreudFilter();

            // 健康
            case HEALTHY:
                return new GLHealthyFilter();

            // 酵母
            case HEFE:
                return new GLHefeFilter();

            // 哈德森
            case HUDSON:
                return new GLHudsonFilter();

            // 凯文
            case KEVIN:
                return new GLKevinFilter();

            // 拿铁
            case LATTE:
                return new GLLatteFilter();

            // LOMO
            case LOMO:
                return new GLLomoFilter();

            // 怀旧之情
            case NOSTALGIA:
                return new GLNostalgiaFilter();

            // 浪漫
            case ROMANCE:
                return new GLRomanceFilter();

            // 樱花
            case SAKURA:
                return new GLSakuraFilter();

            //  素描
            case SKETCH:
                return new GLSketchFilter();

            // 日落
            case SUNSET:
                return new GLSunsetFilter();

            // 白猫
            case WHITECAT:
                return new GLWhiteCatFilter();

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
            // 彩妆滤镜组
            case MAKEUP:
                return new GLMakeUpFilterGroup();

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
