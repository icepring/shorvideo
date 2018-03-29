package com.tym.shortvideo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


import com.tym.shortvideo.interfaces.SingleCallback;
import com.tym.shortvideo.media.VideoInfo;
import com.tym.shortvideo.utils.TrimVideoUtil;
import com.tym.shortvideo.view.SpacesItemDecoration;

import java.util.ArrayList;


/**
 * @author Jliuer
 * @Date 18/03/28 11:25
 * @Email Jliuer@aliyun.com
 * @Description 
 */
public class VideoSelectActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<VideoInfo> allVideos = new ArrayList<>();
    private String videoPath;
    private RecyclerView mRecyclerView;
    private TextView next_step;
    private VideoGridViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.video_select_layout);
        mRecyclerView = findViewById(R.id.video_select_recyclerview);
        next_step = findViewById(R.id.next_step);
        GridLayoutManager manager = new GridLayoutManager(this, 4);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(5));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new VideoGridViewAdapter(this, allVideos);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(manager);

        next_step.setOnClickListener(this);

        next_step.setTextAppearance(this, R.style.gray_text_18_style);
        next_step.setEnabled(false);

        mAdapter.setItemClickCallback(new SingleCallback<Boolean, VideoInfo>() {
            @Override
            public void onSingleCallback(Boolean isSelected, VideoInfo video) {
                if (video != null) {
                    videoPath = video.getPath();
                }
                next_step.setEnabled(isSelected);
                next_step.setTextAppearance(VideoSelectActivity.this, isSelected ? R.style.blue_text_18_style : R.style.gray_text_18_style);
            }
        });

        TrimVideoUtil.getAllVideoFiles(this, new SingleCallback<ArrayList<VideoInfo>, Integer>() {
            @Override
            public void onSingleCallback(ArrayList<VideoInfo> videoInfos, Integer integer) {
                allVideos.clear();
                allVideos.addAll(videoInfos);
                mAdapter.notifyDataSetChanged();
            }
        });

//        FileUtils.createVideoTempFolder();//Create temp file folder
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        allVideos = null;
    }

    @Override
    public void onClick(View v) {
        TrimmerActivity.go(VideoSelectActivity.this, videoPath);
    }
}
