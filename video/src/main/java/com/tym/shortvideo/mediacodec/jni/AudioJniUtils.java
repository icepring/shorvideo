package com.tym.shortvideo.mediacodec.jni;

/**
 * Created by cj on 2017/10/11.
 * desc
 */

public class AudioJniUtils {


    static {
        System.loadLibrary("native-lib");
    }
    public static native byte[] audioMix(byte[] sourceA,byte[] sourceB,byte[] dst,float firstVol , float secondVol);
}
