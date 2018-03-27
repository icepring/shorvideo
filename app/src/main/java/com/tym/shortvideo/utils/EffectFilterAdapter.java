package com.tym.shortvideo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.tym.shortvideo.R;
import com.tym.shortvideo.filter.helper.type.GLFilterType;
import com.tym.shortvideo.view.SquareImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜显示适配器
 * Created by cain.huang on 2017/11/17.
 */

public class EffectFilterAdapter extends RecyclerView.Adapter<EffectFilterAdapter.ImageHolder> {

    private static final String TAG = "EffectFilterAdapter";
    private boolean isDebug = true;

    private Context mContext;
    // 滤镜类型
    private List<GLFilterType> mGlFilterTypes;
    // 滤镜名称
    private List<String> mFilterNames;
    // 滤镜显示图片
    private List<WeakReference<Bitmap>> mWeakBitmaps = new ArrayList<>();

    // 监听器
    private OnItemClickLitener mLitener;

    public EffectFilterAdapter(Context context,
                               List<GLFilterType> glFilterTypes,
                               List<String> filterNames) {
        mContext = context;
        mGlFilterTypes = glFilterTypes;
        mFilterNames = filterNames;
        // 初始化持有的数量，解决onBindViewHolder可能会出现数组越界的情况
        for (int i = 0; i < mGlFilterTypes.size(); i++) {
            mWeakBitmaps.add(i, null);
        }
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_effect_view, null);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        // 设置滤镜图片，这里防止重复加载
        if (mGlFilterTypes != null && !TextUtils.isEmpty(mGlFilterTypes.get(position).toString())) {
            if (mWeakBitmaps.size() <= position
                    || mWeakBitmaps.get(position) == null
                    || mWeakBitmaps.get(position).get() == null
                    || mWeakBitmaps.get(position).get().isRecycled()) {
                String path = "thumbs/" + mGlFilterTypes.get(position).name().toLowerCase() + ".jpg";
                Bitmap bitmap = BitmapUtils.getImageFromAssetsFile(mContext, path);
                if (bitmap != null) {
                    mWeakBitmaps.add(position, new WeakReference<>(bitmap));
                    holder.mImageView.setImageBitmap(bitmap);
                }
                if (isDebug) {
                    Log.d(TAG, "create new filter bitmaps.");
                }
            } else {
                holder.mImageView.setImageBitmap(mWeakBitmaps.get(position).get());
            }

        }
        // 设置滤镜名称
        if (mFilterNames != null && !TextUtils.isEmpty(mFilterNames.get(position))) {
            holder.mTextView.setText(mFilterNames.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return (mGlFilterTypes == null) ? 0 : mGlFilterTypes.size();
    }

    /**
     * 设置点击事件回调
     * @param litener
     */
    public void addItemClickListener(OnItemClickLitener litener) {
        mLitener = litener;
    }

    // 点击事件回调
    public interface OnItemClickLitener {

        void onItemClick(int position); // 单选
    }

    class ImageHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        // 预览缩略图
        public SquareImageView mImageView;
        // 预览文字
        public TextView mTextView;


        public ImageHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.effect_name);
            mImageView = (SquareImageView) itemView.findViewById(R.id.effect_image);
            mImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.effect_image:
                    processSingleSelected();
                    break;
            }
        }

        /**
         * 处理单选
         */
        private void processSingleSelected() {
            if (mLitener != null) {
                mLitener.onItemClick(getLayoutPosition());
            }
        }
    }
}
