package com.tym.shortvideo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.tym.shortvideo.filter.helper.MagicFilterType;
import com.tym.shortvideo.media.MediaPlayerWrapper;
import com.tym.shortvideo.media.VideoInfo;
import com.tym.shortvideo.mediacodec.VideoClipper;
import com.tym.shortvideo.recodrender.ParamsManager;
import com.tym.shortvideo.recordcore.VideoListManager;
import com.tym.shortvideo.recordcore.multimedia.VideoCombineManager;
import com.tym.shortvideo.recordcore.multimedia.VideoCombiner;
import com.tym.shortvideo.utils.FileUtils;
import com.tym.shortvideo.filter.helper.SlideGpuFilterGroup;
import com.tym.shortvideo.view.VideoPreviewView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 * @author Jliuer
 * @Date 18/03/28 10:24
 * @Email Jliuer@aliyun.com
 * @Description 预览
 */
public class PreviewActivity extends BaseActivity implements View.OnClickListener, MediaPlayerWrapper.IMediaCallback,
        SlideGpuFilterGroup.OnFilterChangeListener, View.OnTouchListener {


    private VideoPreviewView mVideoView;
    private String mPath;
    private boolean resumed;
    private boolean isDestroy;
    private boolean isPlaying = false;

    private TextView mFpsView;
    private TextView mConfim;
    private Button mBtnLocal;
    private Button mBtnSwitch;
    private Button mBtnLvjing;
    private Button mBtnBeauty;

    int startPoint;

    private String outputPath;
    static final int VIDEO_PREPARE = 0;
    static final int VIDEO_START = 1;
    static final int VIDEO_UPDATE = 2;
    static final int VIDEO_PAUSE = 3;
    static final int VIDEO_CUT_FINISH = 4;
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VIDEO_PREPARE:
                    Executors.newSingleThreadExecutor().execute(update);
                    break;
                case VIDEO_START:
                    isPlaying = true;
                    break;
                case VIDEO_UPDATE:
                  /*  int curDuration = mVideoView.getCurDuration();
                    if (curDuration > startPoint + clipDur) {
                        mVideoView.seekTo(startPoint);
                        mVideoView.start();
                    }*/
                    break;
                case VIDEO_PAUSE:
                    isPlaying = false;
                    break;
                case VIDEO_CUT_FINISH:
                    Toast.makeText(PreviewActivity.this, "视频保存地址   " + outputPath, Toast.LENGTH_SHORT).show();
                    endLoading();
                    String fileName = (String) msg.obj;

                    FileUtils.updateMediaStore(PreviewActivity.this, outputPath, fileName);

                    TrimmerActivity.go(PreviewActivity.this, outputPath);

                    //TODO　已经渲染完毕了　

                    break;
                default:
            }
        }
    };
    private ImageView mBeauty;
    private MagicFilterType filterType = MagicFilterType.NONE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        initView();
        initData();
    }

    private void initView() {
        mVideoView = (VideoPreviewView) findViewById(R.id.videoView);

        mBtnSwitch = findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);

        mConfim = findViewById(R.id.tv_confrim);
        mConfim.setOnClickListener(this);
        mBtnSwitch.setVisibility(View.GONE);
        mBtnLvjing = findViewById(R.id.btn_lvjing);
        mBtnLvjing.setOnClickListener(this);
        mBtnLvjing.setVisibility(View.GONE);
        mBtnLvjing.setVisibility(View.GONE);
        mBtnBeauty = findViewById(R.id.btn_beauty);
        mBtnBeauty.setOnClickListener(this);

        mVideoView.setOnFilterChangeListener(this);
        mVideoView.setOnTouchListener(this);
        setLoadingCancelable(false);

    }

    private void initData() {
        Intent intent = getIntent();
        //选择的视频的本地播放地址
//        mPath = intent.getStringExtra("path");
        ArrayList<String> srcList = intent.getStringArrayListExtra("path");
//        srcList.add(mPath);
        mVideoView.setVideoPath(srcList);
        mVideoView.setIMediaCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, R.string.change_filter, Toast.LENGTH_SHORT).show();
        if (resumed) {
            mVideoView.start();
        }
        resumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        mVideoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        isDestroy = true;
        mVideoView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!isLoading()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.iv_back:
//            case R.id.iv_close:
//                if (isLoading()) {
//                    endLoading();
//                }
//                finish();
//                break;
            case R.id.btn_beauty:
                mVideoView.switchBeauty();
                if (mBtnBeauty.isSelected()) {
                    mBtnBeauty.setSelected(false);
                } else {
                    mBtnBeauty.setSelected(true);
                }
                break;
            case R.id.tv_confrim:
                if (isLoading()) {
                    return;
                }
                mVideoView.pause();
                showLoading("视频处理中", false);
                combineVideo();



                break;
            default:

        }
    }

    @Override
    public void onVideoPrepare() {
        mHandler.sendEmptyMessage(VIDEO_PREPARE);
    }

    @Override
    public void onVideoStart() {
        mHandler.sendEmptyMessage(VIDEO_START);
    }

    @Override
    public void onVideoPause() {
        mHandler.sendEmptyMessage(VIDEO_PAUSE);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mVideoView.seekTo(startPoint);
        mVideoView.start();
    }

    @Override
    public void onVideoChanged(VideoInfo info) {

    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            while (!isDestroy) {
                if (!isPlaying) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                mHandler.sendEmptyMessage(VIDEO_UPDATE);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onFilterChange(final MagicFilterType type) {
        this.filterType = type;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PreviewActivity.this, "滤镜切换为---" + type, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mVideoView.onTouch(event);
        return true;
    }

    /**
     * 合并视频
     */
    private void combineVideo() {
        final String fileName = "CainCamera_" + System.currentTimeMillis() + ".mp4";
        final String path = ParamsManager.AlbumPath
                + fileName;
        final File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        VideoCombineManager.getInstance()
                .startVideoCombiner(VideoListManager.getInstance().getSubVideoPathList(),
                        path, new VideoCombiner.VideoCombineListener() {
                            @Override
                            public void onCombineStart() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PreviewActivity.this, "开始合并", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                            @Override
                            public void onCombineProcessing(final int current, final int sum) {


                            }

                            @Override
                            public void onCombineFinished(final boolean success) {


                                VideoListManager.getInstance().removeAllSubVideo();
                                // 更更新媒体库
                                FileUtils.updateMediaStore(PreviewActivity.this, path, fileName);

                                VideoClipper clipper = new VideoClipper();
                                clipper.setInputVideoPath(PreviewActivity.this,path);
                                final String fileName = "tym_" + System.currentTimeMillis() + ".mp4";
                                outputPath = FileUtils.getPath("tym/tym/", fileName);
                                clipper.setFilterType(filterType);
                                clipper.setOutputVideoPath(outputPath);
                                clipper.setOnVideoCutFinishListener(new VideoClipper.OnVideoCutFinishListener() {
                                    @Override
                                    public void onFinish() {
                                        mHandler.sendMessage(mHandler.obtainMessage(VIDEO_CUT_FINISH, fileName));
                                    }
                                });
                                try {
                                    clipper.clipVideo(0, mVideoView.getVideoDuration() * 1000);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
    }
}
