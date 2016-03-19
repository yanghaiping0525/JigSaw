package com.yang.jigsaw.camera.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;


/**
 * Created by YangHaiPing on 2016/2/11.
 */
public class FocusView extends View {
    //画对焦圈的画笔
    private Paint mLinePaint;
    //对焦圈的粗细
    private int mBorderWidth = 4;
    //FocusView将要执行的动画集
    private AnimatorSet animatorSet;
    private ObjectAnimator fadeInOut;
    //是否正在对焦标志
    private boolean isFocusing = false;

    public FocusView(Context context) {
        this(context, null);
    }

    public FocusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        //抗锯齿
        mLinePaint.setAntiAlias(true);
        //画空心图案(这里是空心圆)
        mLinePaint.setStyle(Paint.Style.STROKE);
        //画笔颜色(对焦圈颜色)
        mLinePaint.setColor(Color.parseColor("#45e0e0e0"));
        //空心圆宽度
        mLinePaint.setStrokeWidth(mBorderWidth);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //暂时不显示
            this.setAlpha(0f);  //初始化设置透明
        }
    }

    //设置对焦成功后对焦圈的颜色
    private void setMainColor() {
        mLinePaint.setColor(Color.parseColor("#52ce90"));
        postInvalidate();
    }

    //恢复对焦圈原来的颜色
    private void reSet() {
        mLinePaint.setColor(Color.parseColor("#45e0e0e0"));
        //重画
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画个空心圆(根据在布局文件设置的位置和宽高画出相应位置大小的圆)
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 2 - mBorderWidth / 2, mLinePaint);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void beginFocus() {
        isFocusing = true;
        if (animatorSet == null) {
            //对焦时候实现对焦圈的点击后放大直径1.3倍再复原
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.3f, 1f);
            animatorSet = new AnimatorSet();
            animatorSet.play(scaleX).with(scaleY);
            //线性动画效果
            animatorSet.setInterpolator(new LinearInterpolator());
            //动画时长
            animatorSet.setDuration(1000);
            //监听动画事件
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    FocusView.this.setAlpha(1f);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    //显示对焦成功的颜色
                    setMainColor();
                    if (fadeInOut == null) {
                        //动画结束后,慢慢隐藏对焦圈
                        fadeInOut = ObjectAnimator.ofFloat(FocusView.this, "alpha", 1f, 0f);
                        //隐藏持续时间
                        fadeInOut.setDuration(500);
                        fadeInOut.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                //隐藏结束后还原成原来颜色
                                reSet();
                                isFocusing = false;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
                    }
                    fadeInOut.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            //如果在点击屏幕对焦的时候上次的对焦动画还在执行则先取消
            if (animatorSet.isRunning()) {
                animatorSet.cancel();
            }
            if (fadeInOut != null && fadeInOut.isRunning()) {
                fadeInOut.cancel();
            }
        }
        animatorSet.start();
    }

    public boolean isFocusing() {
        return isFocusing;
    }
}
