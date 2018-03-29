package com.tym.shortvideo;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import com.tym.shortvideo.recodrender.ParamsManager;


public class MyApplication extends Application {
    private static Context mContext;
    public static int screenWidth;
    public static int screenHeight;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;
        ParamsManager.context = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
