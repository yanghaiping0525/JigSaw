package com.yang.jigsaw.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

import com.yang.jigsaw.R;

public class IndexScroller {
    private static final int STATE_HIDDEN = 0;
    private static final int STATE_SHOWING = 1;
    private static final int STATE_SHOWN = 2;
    private static final int STATE_HIDING = 3;
    //索引条宽度
    private float mIndexBarWidth;
    private float mIndexBarMargin;
    private float mPreviewPadding;
    private float mDensity; // 当前屏幕密度除以160
    private float mScaledDensity; // 当前屏幕密度除以160(设置字体的尺寸)
    //设置透明度
    private float mAlphaRate;
    //索引条状态
    private int mState = STATE_HIDDEN;
    //ListView的宽高
    private int mListViewWidth;
    private int mListViewHeight;
    //当前索引的位置
    private int mCurrentSection = -1;
    //是否索引中标志
    private boolean isIndexing = false;
    private ListView mListView = null;
    //索引器
    private SectionIndexer mIndexer;
    //索引文字
    private String[] mSections = null;
    //索引条区域
    private RectF mIndexBarRectF = null;
    private static final int WHAT = 0x33;

    public IndexScroller(Context context, ListView listView) {
        mDensity = context.getResources().getDisplayMetrics().density;
        mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        mListView = listView;
        //ListView增加头部View后Adapter会强制转化成HeaderViewListAdapter
        HeaderViewListAdapter hAdapter = (HeaderViewListAdapter) mListView.getAdapter();
        setAdapter((SectionIndexer) hAdapter.getWrappedAdapter());
        //ListView未增加头部View前
        //setAdapter((SectionIndexer)mListView.getAdapter());
        mIndexBarWidth = 20 * mDensity;
        mIndexBarMargin = 30 * mDensity;
        mPreviewPadding = 5 * mDensity;
    }

    public void setAdapter(SectionIndexer adapter) {
        mIndexer = adapter;
        mSections = (String[]) mIndexer.getSections();
    }

    public void draw(Canvas canvas) {
        if (mState == STATE_HIDDEN) {
            return;
        }
        Paint indexBgPaint = new Paint();
        indexBgPaint.setColor(Color.TRANSPARENT);
        //indexBgPaint.setAlpha((int) (64 * mAlphaRate));
        indexBgPaint.setAntiAlias(true);
        if (mIndexBarRectF == null) {
            throw new RuntimeException("indexBarRectF 初始化失败");
        }
        //画出索引条的圆角矩形背景
        canvas.drawRoundRect(mIndexBarRectF, 5 * mDensity, 5 * mDensity,
                indexBgPaint);
        //画出索引条上的索引文字
        Paint indexTextPaint = new Paint();
        indexTextPaint.setColor(Color.GRAY);
        indexTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        //indexTextPaint.setAlpha((int) (255 * mAlphaRate));
        indexTextPaint.setAlpha(127);
        indexTextPaint.setTextSize(14 * mScaledDensity);
        //计算出每块索引区域占据的高度
        float sectionHeight = (mIndexBarRectF.height() - 2 * mIndexBarMargin)
                / mSections.length;
        //计算出文字的PaddingTop用于定位文字
        float paddingTop = (sectionHeight - (indexTextPaint.descent() - indexTextPaint
                .ascent())) / 2;
        //画出所有的索引文字
        for (int i = 0; i < mSections.length; i++) {
            float paddingLeft = (mIndexBarWidth - indexTextPaint
                    .measureText(mSections[i])) / 2;
            canvas.drawText(mSections[i], mIndexBarRectF.left + paddingLeft,
                    mIndexBarRectF.top + mIndexBarMargin + sectionHeight * i
                            + paddingTop - indexTextPaint.ascent(),
                    indexTextPaint);
        }
        //如果索引条被点击，画出预览字母在ListView的正中间
        if (mSections != null && mSections.length > 0) {
            //如果点击了索引条
            if (mCurrentSection >= 0) {
                //画出预览字母的圆角背景的画笔
                Paint previewBgPaint = new Paint();
                previewBgPaint.setColor(Color.RED);
                previewBgPaint.setAlpha(96);
                previewBgPaint.setAntiAlias(true);
                previewBgPaint.setShadowLayer(3, 0, 0, Color.argb(64, 0, 0, 0));
                //画出预览字母的画笔
                Paint previewTextPaint = new Paint();
                previewTextPaint.setColor(Color.WHITE);
                previewTextPaint.setAntiAlias(true);
                previewTextPaint.setTextSize(50 * mScaledDensity);
                //根据文字内容计算出文字宽高
                float textWidth = previewTextPaint
                        .measureText(mSections[mCurrentSection]);
                float textHeight = previewTextPaint.descent()
                        - previewTextPaint.ascent();
                //计算出预览字母背景的大小
                float backgroundSize = 2 * mPreviewPadding + textHeight;
                //确定预览字母背景的绘制区域
                RectF rectF = new RectF((mListViewWidth - backgroundSize) / 2,
                        (mListViewHeight - backgroundSize) / 2,
                        (mListViewWidth + backgroundSize) / 2,
                        (mListViewHeight + backgroundSize) / 2);
                //画出预览字母的圆角背景
                canvas.drawRoundRect(rectF, 5 * mDensity, 5 * mDensity,
                        previewBgPaint);
                //画出预览字母
                canvas.drawText(mSections[mCurrentSection], rectF.left
                                + (backgroundSize - textWidth) / 2, rectF.top
                                + mPreviewPadding - previewTextPaint.ascent(),
                        previewTextPaint);
            }
        }
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mListViewHeight = h;
        mListViewWidth = w;
        mIndexBarRectF = new RectF(w - mIndexBarWidth,
                0, w, h);
    }

    public void show() {
        if (mState == STATE_HIDDEN) {
            setState(STATE_SHOWING);
        } else if (mState == STATE_HIDING) {
            setState(STATE_HIDING);
        }
    }

    private void setState(int state) {
        if (state < STATE_HIDDEN || state > STATE_HIDING) {
            return;
        }
        mState = state;
        switch (mState) {
            case STATE_HIDDEN:
                mHandler.removeMessages(WHAT);
                break;
            case STATE_HIDING:
                mAlphaRate = 1;
                fade(3000);
                break;
            case STATE_SHOWING:
                mAlphaRate = 0;
                fade(0);
                break;
            case STATE_SHOWN:
                mHandler.removeMessages(WHAT);
                break;
        }

    }

    private void fade(long delay) {
        mHandler.removeMessages(WHAT);
        mHandler.sendEmptyMessageAtTime(WHAT, SystemClock.uptimeMillis() + delay);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);

            switch (mState) {
                case STATE_SHOWING:
                    mAlphaRate += (1 - mAlphaRate) * 0.2;
                    if (mAlphaRate > 0.9) {
                        mAlphaRate = 1;
//                        setState(STATE_SHOWN);
                        break;
                    }
                    mListView.invalidate();
                    fade(10);
                    break;
                case STATE_SHOWN:
                    setState(STATE_HIDING);
                    break;
                case STATE_HIDING:
                    mAlphaRate -= mAlphaRate * 0.2;
                    if (mAlphaRate < 0.1) {
                        mAlphaRate = 0;
                        setState(STATE_HIDDEN);
                    }
                    mListView.invalidate();
                    fade(10);
                    break;
            }
        }

    };

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //如果索引条非隐藏状态及点击了索引条
                if (mState != STATE_HIDDEN && contains(ev.getX(), ev.getY())) {
                    //setState(STATE_SHOWN);
                    isIndexing = true;
                    //计算出点击的位置
                    mCurrentSection = getSectionByPoint(ev.getY());
                    //跳转到歌曲名字首字母与索引文字相匹配的首个歌曲的位置,由于ListView添加了head,需要+1
                    mListView.setSelection(mIndexer
                            .getPositionForSection(mCurrentSection) + 1);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isIndexing) {
                    if (contains(ev.getX(), ev.getY())) {
                        mCurrentSection = getSectionByPoint(ev.getY());
                        mListView.setSelection(mIndexer
                                .getPositionForSection(mCurrentSection) + 1);
                    }
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isIndexing) {
                    isIndexing = false;
                    //设置该值为-1后预览文字将不会在Draw方法中被画出来
                    mCurrentSection = -1;
                }
                if (mState == STATE_SHOWN)
                    setState(STATE_HIDING);
                break;
        }
        return false;
    }

    //判断点击的区域是否在索引条范围内
    private boolean contains(float x, float y) {
        return (x >= mIndexBarRectF.left && x <= mIndexBarRectF.right
                && y >= mIndexBarRectF.top && y <= mIndexBarRectF.bottom);
    }

    private int getSectionByPoint(float y) {
        if (mSections == null || mSections.length == 0)
            return 0;
        //如果点击了索引条上方非文字区域返回0
        if (y < mIndexBarRectF.top + mIndexBarMargin)
            return 0;
        //如果点击了索引条下方非文字区域返回最大值
        if (y >= mIndexBarRectF.top + mIndexBarRectF.height() - mIndexBarMargin)
            return mSections.length - 1;
        return (int) ((y - mIndexBarRectF.top - mIndexBarMargin) / ((mIndexBarRectF
                .height() - 2 * mIndexBarMargin) / mSections.length));
    }

    public void hide() {
        if (mState == STATE_SHOWN)
            setState(STATE_HIDING);
    }
}
