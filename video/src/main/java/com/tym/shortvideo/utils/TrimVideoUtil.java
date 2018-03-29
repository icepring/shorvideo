package com.tym.shortvideo.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.tym.shortvideo.filter.helper.MagicFilterType;
import com.tym.shortvideo.interfaces.SingleCallback;
import com.tym.shortvideo.interfaces.TrimVideoListener;
import com.tym.shortvideo.media.VideoInfo;
import com.tym.shortvideo.mediacodec.VideoClipper;

import java.io.IOException;
import java.util.ArrayList;


public class TrimVideoUtil {

    private static final String TAG = TrimVideoUtil.class.getSimpleName();
    public static final int VIDEO_MAX_DURATION = 15;// 15秒
    public static final int MIN_TIME_FRAME = 3;
    private static final int thumb_Width = (DeviceUtils.getScreenWidth() - DeviceUtils.dipToPX(20)) / VIDEO_MAX_DURATION;
    private static final int thumb_Height = DeviceUtils.dipToPX(60);
    private static final long one_frame_time = 1000000;

    public static void trim(Context context, Uri inputFile, String outputFile, long startMs, long endMs, final TrimVideoListener callback) {
        VideoClipper clipper = new VideoClipper();
        clipper.setFilterType(MagicFilterType.NONE);
        clipper.setInputVideoPath(context,inputFile);
        outputFile = FileUtils.getPath("tym/out/", System.currentTimeMillis() + ".mp4");
        clipper.setOutputVideoPath(outputFile);
        final String tempOutFile = outputFile;





        clipper.setOnVideoCutFinishListener(new VideoClipper.OnVideoCutFinishListener() {
            @Override
            public void onFinish() {
                callback.onFinishTrim(tempOutFile);
            }
        });
        try {
            clipper.clipVideo(startMs, endMs - startMs);
            callback.onStartTrim();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void backgroundShootVideoThumb(final Context context, final Uri videoUri, final SingleCallback<ArrayList<Bitmap>, Integer> callback) {
        final ArrayList<Bitmap> thumbnailList = new ArrayList<>();
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0L, "") {
                                       @Override
                                       public void execute() {
                                           try {
                                               MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                               mediaMetadataRetriever.setDataSource(context, videoUri);
                                               // Retrieve media data use microsecond
                                               long videoLengthInMs = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;
                                               long numThumbs = videoLengthInMs < one_frame_time ? 1 : (videoLengthInMs / one_frame_time);
                                               final long interval = videoLengthInMs / numThumbs;

                                               //每次截取到3帧之后上报
                                               for (long i = 0; i < numThumbs; ++i) {
                                                   Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                                                   try {
                                                       bitmap = Bitmap.createScaledBitmap(bitmap, thumb_Width, thumb_Height, false);
                                                   } catch (Exception e) {
                                                       e.printStackTrace();
                                                   }
                                                   thumbnailList.add(bitmap);
                                                   if (thumbnailList.size() == 3) {
                                                       callback.onSingleCallback((ArrayList<Bitmap>) thumbnailList.clone(), (int) interval);
                                                       thumbnailList.clear();
                                                   }
                                               }
                                               if (thumbnailList.size() > 0) {
                                                   callback.onSingleCallback((ArrayList<Bitmap>) thumbnailList.clone(), (int) interval);
                                                   thumbnailList.clear();
                                               }
                                               mediaMetadataRetriever.release();
                                           } catch (final Throwable e) {
                                               Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                                           }
                                       }
                                   }
        );

    }

    public static String getVideoFilePath(String url) {

        if (TextUtils.isEmpty(url) || url.length() < 5) {
            return "";
        }

        if (url.substring(0, 4).equalsIgnoreCase("http")) {
        } else {
            url = "file://" + url;
        }

        return url;
    }

    private static String convertSecondsToTime(long seconds) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (seconds <= 0) {
            return "00:00";
        } else {
            minute = (int) seconds / 60;
            if (minute < 60) {
                second = (int) seconds % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99) {
                    return "99:59:59";
                }
                minute = minute % 60;
                second = (int) (seconds - hour * 3600 - minute * 60);
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10) {
            retStr = "0" + Integer.toString(i);
        } else {
            retStr = "" + i;
        }
        return retStr;
    }

    public static void getAllVideoFiles(final Context mContext, final SingleCallback<ArrayList<VideoInfo>, Integer> callback) {

        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0L, "") {
                                       @Override
                                       public void execute() {
                                           VideoInfo video;
                                           ArrayList<VideoInfo> videos = new ArrayList<>();



                                           ContentResolver contentResolver = mContext.getContentResolver();
                                           try {
                                               Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                                                       null, null, MediaStore.Video.Media.DATE_MODIFIED + " desc");
                                               if (cursor != null) {
                                                   while (cursor.moveToNext()) {
                                                       video = new VideoInfo();
                                                       if (cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)) != 0) {
                                                           video.setDuration((int) cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
                                                           video.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                                                           video.setCreateTime(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)));
                                                           video.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
                                                           videos.add(video);
                                                       }
                                                   }
                                                   cursor.close();
                                               }
                                               callback.onSingleCallback(videos, 1);
                                           } catch (Exception e) {
                                               callback.onSingleCallback(videos, 0 );
                                               e.printStackTrace();
                                           }
                                       }
                                   }
        );
    }
}
