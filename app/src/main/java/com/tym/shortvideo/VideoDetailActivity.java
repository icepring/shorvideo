package com.tym.shortvideo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * @Author Jliuer
 * @Date 2018/03/29/10:31
 * @Email Jliuer@aliyun.com
 * @Description
 */
public class VideoDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gogogo(View v) {
        startActivity(new Intent(this, CameraActivity.class));

    }
}
