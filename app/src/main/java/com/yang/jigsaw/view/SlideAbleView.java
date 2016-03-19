package com.yang.jigsaw.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.nineoldandroids.view.ViewHelper;
import com.yang.jigsaw.utils.DensityUtil;
import com.yang.jigsaw.utils.ScreenSize;

/**
 * Created by YangHaiPing on 2016/2/25.
 */
public class SlideAbleView extends HorizontalScrollView {
    private int mScreenWidth;
    //包含侧拉菜单栏及主界面的容器
    private LinearLayout mWrapper;
    //侧拉菜单栏(音乐播放界面)
    private ViewGroup mSlideMenu;
    //主界面(游戏界面)
    private ViewGroup mContent;
    //侧拉菜单栏的宽度
    private int mSlideMenuWidth;
    private boolean once;
    //侧拉菜单栏是否打开标志
    private boolean isSlideMenuOpen;
    private int mSlideMenuPaddingRight;
    private float yDown;

    public SlideAbleView(Context context) {
        this(context, null);
    }

    public SlideAbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideAbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = ScreenSize.getWidth(context);
        //侧拉菜单paddingRight 90dp
        mSlideMenuPaddingRight = DensityUtil.dip2px(context, 90);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!once) {
            //初始化侧拉菜单以及主界面的宽度
            mWrapper = (LinearLayout) getChildAt(0);
            mSlideMenu = (ViewGroup) mWrapper.getChildAt(0);
            mContent = (ViewGroup) mWrapper.getChildAt(1);
            mSlideMenuWidth = mSlideMenu.getLayoutParams().width = mScreenWidth - mSlideMenuPaddingRight;
            mContent.getLayoutParams().width = mScreenWidth;
            once = true;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //将Menu隐藏,该方法在onMeasure, onSizeChanged 后调用
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //隐藏侧拉菜单,防止HorizontalScrollView自动滚到起始位置
        if (!isSlideMenuOpen) {
            hideSlideView();
        }
    }


    public void hideSlideView() {
        //隐藏侧拉菜单
        this.scrollTo(mSlideMenuWidth, 0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //如果菜单打开并且触控位置在菜单以外，容器吸收触摸事件,子view不能分发到触摸事件
        if (isSlideMenuOpen && ev.getRawX() > mSlideMenuWidth) {
            return true;
        }
        //如果ListView或者其他控件的上下滚动事件触发,令HorizontalScrollView的水平滚动事件不触发,而是往下传递触摸事件
        else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    yDown = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //避免水平滚动事件和上下滚动事件的冲突
                    if (Math.abs(ev.getY() - yDown) > 20) {
                        return false;
                    }
                    break;
            }
        }

        return super.onInterceptTouchEvent(ev);
    }

    //在ScrollView中ListView的OnTouchEvent中的ACTION_DOWN事件无法响应，得在dispatchTouchEvent中接收
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                //当侧栏栏隐藏时,getScrollX() = mSlideMenuWidth,当主界面左侧拉到屏幕中间往左时隐藏侧拉菜单栏，反之显示侧拉菜单栏
                if (getScrollX() >= mSlideMenuWidth - mScreenWidth / 2) {
                    //缓慢隐藏
                    this.smoothScrollTo(mSlideMenuWidth, 0);
                    isSlideMenuOpen = false;
                } else {
                    //完全显示
                    this.smoothScrollTo(0, 0);
                    isSlideMenuOpen = true;
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    private void openMenu() {
        if (isSlideMenuOpen)
            return;
        this.smoothScrollTo(0, 0);
        isSlideMenuOpen = true;
    }

    private void closeMenu() {
        if (!isSlideMenuOpen)
            return;
        this.smoothScrollTo(mSlideMenuWidth, 0);
        isSlideMenuOpen = false;
    }

    public void toggle() {
        if (isSlideMenuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //l往左滚动增大,往右减小
        float rate = l * 1.0f / mSlideMenuWidth;//往右1~0,往左0~1
        //滚动的同时将侧拉菜单栏往右移动相应距离，实现抽屉效果
        ViewHelper.setTranslationX(mSlideMenu, mSlideMenuWidth * rate);
//        float contentScale = 0.7f + 0.3f * rate;//1~0.7
//        ViewHelper.setScaleX(mContent, contentScale);
//        ViewHelper.setScaleY(mContent, contentScale);
//        ViewHelper.setPivotX(mContent, 0);
//        ViewHelper.setPivotY(mContent, mContent.getHeight() / 2);
        float menuAlpha = 0.5f + 0.5f * (1 - rate);//往右0.5~1,往左1~0.5
        ViewHelper.setAlpha(mSlideMenu, menuAlpha);
    }

}
