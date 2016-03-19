package com.yang.jigsaw.camera.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.yang.jigsaw.camera.adapter.MyEffectSelectedAdapter;
import com.yang.jigsaw.utils.ScreenSize;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by YangHaiPing on 2016/2/13.
 */
public class MyEffectSelectedView extends HorizontalScrollView implements View.OnClickListener {
    //屏幕宽度
    private int mScreenWidth;
    //装载子View的容器
    private LinearLayout mContainer;
    //子View的宽高
    private int mChildWidth;
    private int mChildHeight;
    //跟踪最后面子View的位置
    private int mFinalCursor = 0;
    //跟踪最前面子View的位置
    private int mFirstCursor = 0;
    //自定义适配器
    private MyEffectSelectedAdapter mAdapter;
    //当前屏幕所能显示的最大子View的数量
    public static int mMaxCountOnScreen;
    //记录并保存子View的位置
    private Map<View, Integer> mViewsPositions = new HashMap<>();
    //子View被点击回调接口
    private OnItemClickListener onItemClickListener;
    //子View改变的回调接口
    private OnViewChangeListener onViewChangeListener;
    //当前容器正在载入下一个子View和上一个子View标志
    private boolean isLoadingNext, isLoadingPrevious;
    //完成容器左右滑动改变子View的标志
    private boolean isContinue, isContinue_sec, isContinue_third, isContinue_forth, isContinue_fifth, isContinue_sixth, isContinue_seventh, isContinue_eighth, isContinue_ninth;
    //触摸事件down的x坐标位置
    private float downX;
    //上一次触摸事件移动的距离
    private float lastMove;
    private boolean isIncreasing, isReducing;
    private boolean isOnce;


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnViewChangeListener(OnViewChangeListener onViewChangeListener) {
        this.onViewChangeListener = onViewChangeListener;
    }


    public interface OnItemClickListener {
        void onClick(View view, int pos);
    }

    public interface OnViewChangeListener {
        void onViewChange(View view, int pos);
    }

    public MyEffectSelectedView(Context context) {
        this(context, null);
    }

    public MyEffectSelectedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyEffectSelectedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = ScreenSize.getWidth(context);
    }

    //加载各个效果的子View进容器
    public void setAdapter(MyEffectSelectedAdapter adapter) {
        this.mAdapter = adapter;
        if (getChildCount() > 0 && getChildAt(0) instanceof LinearLayout) {
            mContainer = (LinearLayout) getChildAt(0);
        } else {
            throw new RuntimeException("初始化容器失败");
        }
        //先获得一个效果图icon,用来计算icon的宽高
        final View view = mAdapter.getView(0, null, mContainer);
        mContainer.addView(view);
        //在OnCreate()中获取控件高度与宽度，getWidth()与getHeight()方法返回是0
        if (mChildWidth == 0 || mChildHeight == 0) {
            int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            view.measure(w, h);
            mChildHeight = view.getMeasuredHeight();//包含paddingRight和paddingLeft
            mChildWidth = view.getMeasuredWidth();
        }
        //如果上一步获取宽高失败了使用下面方法
        if (mChildHeight == 0 || mChildWidth == 0) {
            ViewTreeObserver observer = view.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mChildHeight = view.getMeasuredHeight();
                    mChildWidth = view.getMeasuredWidth();
                    return true;
                }
            });
        }
        //如果获取宽高还是失败了则返回
        if (mChildHeight == 0 || mChildWidth == 0)
            return;
        //计算出容器所能容纳的子View的最大值
        mMaxCountOnScreen = mScreenWidth / mChildWidth;
        //如果容器中的子View过少则设置容器容量最大值为子View的数量
        if (mMaxCountOnScreen > mAdapter.getCount())
            mMaxCountOnScreen = mAdapter.getCount();
        //初始化并显示第一页中的效果图标
        initFirstScreenChildren(mMaxCountOnScreen);
    }

    public void initFirstScreenChildren(int maxPreviewCount) {
        mContainer.removeAllViews();
        mViewsPositions.clear();
        for (int i = 0; i < maxPreviewCount; i++) {
            View view = mAdapter.getView(i, null, null);
            view.setOnClickListener(this);
            //将子View加载到容器中
            mContainer.addView(view);
            //记录当前所有子View的位置
            mViewsPositions.put(view, i);
            if (!isOnce) {
                mFinalCursor++;
            }
        }
        isOnce = true;
        if (onViewChangeListener != null) {
            notifyCurrentViewChange();
        }
    }


    protected void loadNextView() {
        isLoadingNext = true;
        //如果finalIndex达到最大值,则表明已经显示到最后
        if (mFinalCursor == mAdapter.getCount()) {
            isLoadingNext = false;
            return;
        } else {
            //在集合中移除第一个子view
            mViewsPositions.remove(mContainer.getChildAt(0));
            //在容器中移除第一个子view
            mContainer.removeViewAt(0);
            //获取下一个子view，并且设置onclick事件，且加入容器中
            View view = mAdapter.getView(mFinalCursor, null, null);
            view.setOnClickListener(this);
            //将新的子View添加到容器的最后一个位置
            mContainer.addView(view, mMaxCountOnScreen - 1);
            //记录被添加的子view的位置
            mViewsPositions.put(view, mFinalCursor);
            mFirstCursor++;
            mFinalCursor++;
            if (onViewChangeListener != null) {
                notifyCurrentViewChange();
            }
        }
        isLoadingNext = false;
    }

    protected void loadPreviousView() {
        isLoadingPrevious = true;
        if (mFirstCursor == 0) {
            isLoadingPrevious = false;
            return;
        } else {
            //获得当前显示中第一个子view的下标
            int index = mFirstCursor - 1;
            if (index >= 0) {
                //在集合中移除最后一个子view
                mViewsPositions.remove(mContainer.getChildAt(mContainer.getChildCount() - 1));
                //在容器中移除最后一个子view
                mContainer.removeViewAt(mMaxCountOnScreen - 1);
                //获取前一个子view
                View view = mAdapter.getView(index, null, null);
                mViewsPositions.put(view, index);
                //将此View放入第一个位置
                mContainer.addView(view, 0);
                view.setOnClickListener(this);
                mFirstCursor--;
                mFinalCursor--;
                if (onViewChangeListener != null) {
                    notifyCurrentViewChange();
                }
            }
        }
        isLoadingPrevious = false;
    }

    private void notifyCurrentViewChange() {
        /*for (int i = 0; i < mContainer.getChildCount(); i++) {
            mContainer.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
        }*/
        onViewChangeListener.onViewChange(mContainer.getChildAt(0), mFinalCursor);
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            for (int i = 0; i < mContainer.getChildCount(); i++) {
                mContainer.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            }
            //将子view对象及位置传递给监听者
            onItemClickListener.onClick(v, mViewsPositions.get(v));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                lastMove = downX;
                isContinue = true;
                isContinue_sec = true;
                isContinue_third = true;
                isContinue_forth = true;
                isContinue_fifth = true;
                isContinue_sixth = true;
                isContinue_seventh = true;
                isContinue_eighth = true;
                isContinue_ninth = true;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX() - downX;
                //判断当前移动的x偏移量和上次偏移量的值比较,判断是往左滑动还是往右滑动
                //往右滑动
                if (moveX - lastMove > 0) {
                    isIncreasing = true;
                    //如果是从原来的往左滑动变成现在的往右滑动,将当前的x值重新定义为起始位置,即当做是手指重新点击屏幕时候的位置,并重置标志位
                    if (isReducing) {
                        downX = ev.getX();
                        reset();
                    }
                    isReducing = false;
                }
                //往左滑动
                else if (moveX - lastMove < 0) {
                    isReducing = true;
                    //如果是从原来的往右滑动变成现在的往左滑动,将当前的x值重新定义为起始位置,即当做是手指重新点击屏幕时候的位置,并重置标志位
                    if (isIncreasing) {
                        downX = ev.getX();
                        reset();
                    }
                    isIncreasing = false;
                }
                //记录刚才滑动的距离
                lastMove = moveX;
                //将屏幕横向分成几段,每滑动一段距离触发加载下一个或上一个子View事件
                if (isContinue && !isLoadingNext && moveX <= -mScreenWidth / 30) {
                    isContinue = false;
                    loadNextView();
                } else if (isContinue && !isLoadingPrevious && moveX >= mScreenWidth / 30) {
                    isContinue = false;
                    loadPreviousView();
                }
                if (isContinue_sec && (moveX <= -mScreenWidth / 15 || moveX >= mScreenWidth / 15)) {
                    isContinue = true;
                    isContinue_sec = false;
                } else if (isContinue_third && (moveX <= -mScreenWidth * 3 / 10 || moveX >= mScreenWidth * 3 / 10)) {
                    isContinue = true;
                    isContinue_third = false;
                } else if (isContinue_forth && (moveX <= -mScreenWidth * 2 / 5 || moveX >= mScreenWidth * 2 / 5)) {
                    isContinue = true;
                    isContinue_forth = false;
                } else if (isContinue_fifth && (moveX <= -mScreenWidth * 1 / 2 || moveX >= mScreenWidth * 1 / 2)) {
                    isContinue = true;
                    isContinue_fifth = false;
                } else if (isContinue_sixth && (moveX <= -mScreenWidth * 3 / 5 || moveX >= mScreenWidth * 3 / 5)) {
                    isContinue = true;
                    isContinue_sixth = false;
                } else if (isContinue_seventh && (moveX <= -mScreenWidth * 7 / 10 || moveX >= mScreenWidth * 7 / 10)) {
                    isContinue = true;
                    isContinue_seventh = false;
                } else if (isContinue_eighth && (moveX <= -mScreenWidth * 4 / 5 || moveX >= mScreenWidth * 4 / 5)) {
                    isContinue = true;
                    isContinue_eighth = false;
                } else if (isContinue_ninth && (moveX <= -mScreenWidth * 9 / 10 || moveX >= mScreenWidth * 9 / 10)) {
                    isContinue = true;
                    isContinue_ninth = false;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void reset() {
        isContinue = true;
        isContinue_sec = true;
        isContinue_third = true;
        isContinue_forth = true;
        isContinue_fifth = true;
        isContinue_sixth = true;
        isContinue_seventh = true;
        isContinue_eighth = true;
        isContinue_ninth = true;
    }

}
