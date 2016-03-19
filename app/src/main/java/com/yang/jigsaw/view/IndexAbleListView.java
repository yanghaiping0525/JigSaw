package com.yang.jigsaw.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

public class IndexAbleListView extends ListView {
    private boolean mIsFastScrollEnabled = false;
    //字母索引条
    private IndexScroller mIndexScroller = null;
    private Context mContext;

    public IndexAbleListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public IndexAbleListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexAbleListView(Context context) {
        this(context, null);
    }

    //如果设置了快速滚动则显示字母索引条
    @Override
    public void setFastScrollEnabled(boolean enabled) {
//        super.setFastScrollEnabled(enabled);//屏蔽ListView自带的快速滚动条
        mIsFastScrollEnabled = enabled;
        if (mIsFastScrollEnabled) {
            if (mIndexScroller == null) {
                mIndexScroller = new IndexScroller(mContext, this);
                mIndexScroller.show();
            }
        } else {
            if (mIndexScroller != null) {
                mIndexScroller.hide();
                mIndexScroller = null;
            }
        }
    }

    //重写draw方法画出索引条
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mIndexScroller != null) {
            mIndexScroller.draw(canvas);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //如果触摸索引条消耗触摸事件,防止ListView跟着滚动
        if (mIndexScroller != null && mIndexScroller.onTouchEvent(ev)) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if (mIndexScroller != null) {
            mIndexScroller.setAdapter((SectionIndexer) adapter);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //通过该方法索引条获得ListView的位置信息
        if (mIndexScroller != null) {
            mIndexScroller.onSizeChanged(w, h, oldw, oldh);
        }
    }

}
