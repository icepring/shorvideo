package com.tym.shortvideo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;


import com.tym.shortvideo.interfaces.TrimVideoListener;
import com.tym.shortvideo.utils.TrimVideoUtil;
import com.tym.shortvideo.view.VideoTrimmerView;

import java.io.File;

public class TrimmerActivity extends AppCompatActivity implements TrimVideoListener {

    private static final String TAG = "jason";
    public static final int VIDEO_TRIM_REQUEST_CODE = 0x001;
    private File tempFile;
    private ProgressDialog mProgressDialog;

    private VideoTrimmerView mVideoTrimmerView;

    public static void go(Activity from, String videoPath) {
        if (!TextUtils.isEmpty(videoPath)) {
            Bundle bundle = new Bundle();
            bundle.putString("path", videoPath);
            Intent intent = new Intent(from, TrimmerActivity.class);
            intent.putExtras(bundle);
            from.startActivityForResult(intent, VIDEO_TRIM_REQUEST_CODE);
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_time);
        Bundle bd = getIntent().getExtras();
        String path = "";
        if (bd != null) {
            path = bd.getString("path");
        }
        mVideoTrimmerView = findViewById(R.id.trimmer_view);
        mVideoTrimmerView.setMaxDuration(TrimVideoUtil.VIDEO_MAX_DURATION);
        mVideoTrimmerView.setOnTrimVideoListener(this);
        mVideoTrimmerView.setVideoURI(Uri.parse(path));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoTrimmerView.onPause();
        mVideoTrimmerView.setRestoreState(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoTrimmerView.destroy();
    }

    @Override
    public void onStartTrim() {
        buildDialog(getResources().getString(R.string.trimming)).show();
    }

    @Override
    public void onFinishTrim(String in) {
        //TODO: please handle your trimmed video url here!!!
        String out = "/storage/emulated/0/Android/data/com.iknow.android/cache/compress.mp4";
        Log.d("onFinishTrim::",in);
        mProgressDialog.dismiss();
    }

    @Override
    public void onCancel() {
        mVideoTrimmerView.destroy();
        finish();
    }

    private ProgressDialog buildDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", msg);
        }
        mProgressDialog.setMessage(msg);
        return mProgressDialog;
    }
}
