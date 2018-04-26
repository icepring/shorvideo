package com.tym.shortvideo;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.tym.shortvideo.interfaces.SingleCallback;
import com.tym.shortvideo.media.MediaPlayerWrapper;
import com.tym.shortvideo.media.VideoInfo;
import com.tym.shortvideo.utils.TrimVideoUtil;
import com.tym.shortvideo.utils.UiThreadExecutor;
import com.tym.shortvideo.view.VideoCoverView;
import com.tym.shortvideo.view.VideoPreviewView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Jliuer
 * @Date 2018/03/29/10:31
 * @Email Jliuer@aliyun.com
 * @Description
 */
public class VideoDetailActivity extends AppCompatActivity implements MediaPlayerWrapper.IMediaCallback {

    private VideoCoverView mVideoCoverView;
    private VideoPreviewView mVideoView;
    private ImageView mCoverImage;
    private String path;
    private Uri uri;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoCoverView = findViewById(R.id.rl_bottom_container);
        mVideoView = findViewById(R.id.videoView);

//        gogogo();

        path = "/storage/emulated/0/DCIM/Camera/VID_20180425_170450.mp4";
        uri = Uri.parse(path);
        List<String> stringList=new ArrayList<>();
        stringList.add(path);
        mVideoView.setVideoPath(stringList);
        mVideoView.setIMediaCallback(this);
        mVideoCoverView.setScrollDistanceToTime(new VideoCoverView.ScrollDistanceToTime() {
            @Override
            public void changeTo(long millisecond) {

                mVideoView.seekTo((int) millisecond);
                mVideoView.takePic(new SingleCallback<Bitmap, Integer>() {
                    @Override
                    public void onSingleCallback(final Bitmap bitmap, Integer integer) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mVideoCoverView.setImageBitmap(bitmap);
                            }
                        });
                    }
                });
            }
        });
    }

    public void gogogo() {

//        startActivity(new Intent(this, CameraActivity.class));

    }

    @Override
    public void onVideoPrepare() {
        TrimVideoUtil.backgroundShootVideoThumb(this, uri, new SingleCallback<ArrayList<Bitmap>, Integer>() {
            @Override
            public void onSingleCallback(final ArrayList<Bitmap> bitmap, final Integer interval) {
                UiThreadExecutor.runTask("", new Runnable() {
                    @Override
                    public void run() {
                        mVideoCoverView.addImages(bitmap);
                    }
                }, 0L);

            }
        });
    }

    @Override
    public void onVideoStart() {

    }

    @Override
    public void onVideoPause() {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mVideoView.seekTo(0);
        mVideoView.start();
    }

    @Override
    public void onVideoChanged(VideoInfo info) {

    }
}
