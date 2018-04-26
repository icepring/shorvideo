package com.tym.shortvideo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tym.shortvideo.utils.TrimVideoUtil;
import com.tym.video.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Jliuer
 * @Date 2018/04/26/11:49
 * @Email Jliuer@aliyun.com
 * @Description 1s 截图 2 张
 */
public class VideoCoverView extends RelativeLayout {

    private ImageView mCoverView;
    private RecyclerView mCoverListView;
    private ViewDragHelper mDragHelper;
    private CoverAdapter mAdapter;

    private int mXOffset;
    private int mYOffset;

    private ScrollDistanceToTime mScrollDistanceToTime;

    private static final float TIME = 2.4f;
    private int mCoverViewWidth;
    private float mCurrentStartTime;

    private int oldLeft;

    public VideoCoverView(@NonNull Context context) {
        this(context, null);
    }

    public VideoCoverView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoCoverView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDragHelper = ViewDragHelper.create(this, 10000.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == mCoverView;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return mCoverView.getTop();
            }

            /**
             *
             * @param child
             * @param left 将要移动到的位置的坐标
             * @param dx
             * @return
             */
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound = mCoverListView.getPaddingLeft();
                final int rightBound = getWidth() - mCoverView.getWidth() - leftBound;
                return Math.min(Math.max(left, leftBound), rightBound);
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return 0;
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                Log.d("onViewPositionChanged::", left + "");
                int relativePosition = left + changedView.getWidth() - mCoverListView.getPaddingLeft();
                float time = TIME * relativePosition / mCoverViewWidth;
                int frame = (int) ((mCurrentStartTime + time) * 1000);
                Log.d("onViewPositionChanged::frame::", frame + "");
                if (mScrollDistanceToTime != null && Math.abs(left - oldLeft) >= 20) {
                    oldLeft = left;
                    mScrollDistanceToTime.changeTo(frame);
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
//        if (mXOffset != 0 && mCoverView.getLeft() != mXOffset) {
//            mCoverView.offsetLeftAndRight(mXOffset);
//            mCoverView.offsetTopAndBottom(mYOffset);
//        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        } else {
            mXOffset = mCoverView.getLeft();
            mYOffset = mCoverView.getTop();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCoverView = ((ImageView) findViewById(R.id.iv_cover));
        mCoverListView = (RecyclerView) findViewById(R.id.rl_cover_list);
        mAdapter = new CoverAdapter();
        mCoverListView.setAdapter(mAdapter);
        mCoverListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mCoverView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });

        mCoverListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mScrollDistanceToTime != null) {
                    // 这里有padding，并没有重 0 开始
                    mCoverViewWidth = mCoverListView.getWidth() - 2 * mCoverListView.getPaddingLeft();
                    int distance = getScollXDistance() + mCoverListView.getPaddingLeft();
                    mCurrentStartTime = TIME * distance / mCoverViewWidth;
                    Log.d("distance:", distance + "");
                    Log.d("time:", mCurrentStartTime + "");
                    mScrollDistanceToTime.changeTo((int) mCurrentStartTime * 1000);
                }

            }
        });
    }

    public void addImages(List<Bitmap> coverList) {
        if (mAdapter != null) {
            mAdapter.addImages(coverList);
        }
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        if (mCoverView!=null){
            mCoverView.setImageBitmap(imageBitmap);
        }
    }

    class CoverAdapter extends RecyclerView.Adapter<Holder> {

        private List<Bitmap> coverList = new ArrayList<>();

        public CoverAdapter() {
        }

        public CoverAdapter(List<Bitmap> coverList) {
            this.coverList = coverList;
        }

        public void addImages(List<Bitmap> coverList) {
            this.coverList.addAll(coverList);
            notifyDataSetChanged();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_thumb_itme_layout, null);
            return new Holder(convertView);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.mImageView.setImageBitmap(coverList.get(position));
        }

        @Override
        public int getItemCount() {
            return coverList.size();
        }
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.thumb);
        }
    }

    /**
     * 要求 每个item View的高度一致，不然就判断 类型，自己加宽高
     *
     * @return
     */
    private int getScollXDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mCoverListView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisiableChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisiableChildView.getWidth();
        return (position) * itemWidth - firstVisiableChildView.getLeft();
    }

    public void setScrollDistanceToTime(ScrollDistanceToTime scrollDistanceToTime) {
        mScrollDistanceToTime = scrollDistanceToTime;
    }

    public ImageView getCoverView() {
        return mCoverView;
    }

    public interface ScrollDistanceToTime {
        /**
         * 时间都去哪儿了
         *
         * @param millisecond
         */
        void changeTo(long millisecond);
    }
}
