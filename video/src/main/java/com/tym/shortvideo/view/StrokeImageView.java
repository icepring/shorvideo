package com.tym.shortvideo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @Author Jliuer
 * @Date 2018/04/26/18:51
 * @Email Jliuer@aliyun.com
 * @Description
 */
public class StrokeImageView extends AppCompatImageView {

    private Rect mStrokeRect;
    private Paint mPaint;

    public StrokeImageView(Context context) {
        this(context, null);
    }

    public StrokeImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrokeImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mStrokeRect = new Rect();
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.getClipBounds(mStrokeRect);
        mStrokeRect.inset(2, 2);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        canvas.drawRect(mStrokeRect, mPaint);
    }
}
