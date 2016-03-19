package com.yang.jigsaw.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.yang.jigsaw.R;


/**
 * Created by Administrator on 2016/1/29.
 */
public class ZoomImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {
    private boolean mOnce;
    private float mInitScale;
    private float mDoubleClickScale;
    private float mMaxScale;
    private Matrix mScaleMatrix;
    private ScaleGestureDetector mScaleGestureDetector;
    private int mLastPointerCount;
    private float mLastCenterX;
    private float mLastCenterY;
    private float mTouchSlop;
    private boolean isCanDrag;
    private boolean isCheckedLeftAndRight;
    private boolean isCheckedTopAndBottom;
    private GestureDetector mGestureDetector;
    private boolean isAutoScale;
    private boolean isAutoTranslate;
    private int mMaxScaleTimes = 4;
    private int mDoubleClickScaleTimes = 2;
    private onLongPressListener mListener;
    private Context mContext;
    private Drawable drawable;

    public interface onLongPressListener {
        void longPress();
    }

    public void setOnLongPressListener(onLongPressListener listener) {
        this.mListener = listener;
    }

    public void setMaxScaleTimes(int maxScaleTimes) {
        if (maxScaleTimes > 10 || maxScaleTimes <= 0)
            return;
        this.mMaxScaleTimes = maxScaleTimes;
    }

    public void setDoubleClickScaleTimes(int doubleClickScaleTimes) {
        if (doubleClickScaleTimes > 5 || doubleClickScaleTimes <= 1)
            return;
        this.mDoubleClickScaleTimes = doubleClickScaleTimes;
    }


    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ZoomImage);
        mMaxScaleTimes = typedArray.getInteger(R.styleable.ZoomImage_MaxScaleTimes, 4);
        mDoubleClickScaleTimes = typedArray.getInteger(R.styleable.ZoomImage_DoubleClickScaleTimes, 2);
        typedArray.recycle();
        mScaleMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
        //getScaledTouchSlop是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件。如果小于这个距离就不触发移动控件，如viewpager就是用这个距离来判断用户是否翻页
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isAutoScale)
                    return true;
                float px = e.getX();
                float py = e.getY();
                if (getCurrentScale() < mDoubleClickScale) {
                    isAutoScale = true;
                    postDelayed(new AutoScaleRunnable(mDoubleClickScale,
                            px, py), 10);
                } else {
                    isAutoScale = true;
                    postDelayed(
                            new AutoScaleRunnable(mInitScale, px, py),
                            10);
                }
                return true;//return true表示事件继续
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (mListener != null) {
                    mListener.longPress();
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (isAutoTranslate) {
                    return true;
                }
                if (getCurrentScale() == mInitScale) {
                    return true;
                }
                if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
                    return true;
                }
                if (Math.abs(velocityX) < getWidth() * 3 && Math.abs(velocityY) < getHeight() * 3) {
                    return mGestureDetector.onTouchEvent(e1);
                }
                RectF rectF = getMatrixRectF();
                float _dx = -getCurrentTranslateX() / (drawable.getIntrinsicWidth() * getCurrentScale());
                float _dy = -getCurrentTranslateY() / (drawable.getIntrinsicHeight() * getCurrentScale());
                float dx = _dx * velocityX / 3;
                float dy = _dy * velocityY / 3;
                // Log.i("_dd", "velocityX = " + velocityX + "  velocityY = " + velocityY);
                // Log.i("_dd", "dx = " + dx + "  dy = " + dy);
                if (rectF.width() > getWidth() + 0.01
                        || rectF.height() > getHeight() + 0.01) {
                    if (getParent() instanceof ViewPager)
                        getParent().requestDisallowInterceptTouchEvent(true);
                }
                isCheckedLeftAndRight = isCheckedTopAndBottom = true;

                if (rectF.width() < getWidth() || Math.abs(velocityX) < getWidth() * 3) {
                    // isCheckedLeftAndRight = false;
                    dx = 0f;
                }
                if (rectF.height() < getHeight() || Math.abs(velocityY) < getHeight() * 3) {
                    // isCheckedTopAndBottom = false;
                    dy = 0f;
                }

                isAutoTranslate = true;
                final int finalDx = (int) dx;
                final int finalDy = (int) dy;
                postDelayed(new AutoTranslateRunnable(finalDx, finalDy), 10);

                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

    }

    private class AutoTranslateRunnable implements Runnable {
        private int dx;
        private int dy;
        private int dx_ratio;
        private int dy_ratio;
        private int temp_dx;
        private int temp_dy;
        private final float ratio = 0.2f;

        public AutoTranslateRunnable(int dx, int dy) {
            this.dx_ratio = (int) (dx * ratio);
            this.dy_ratio = (int) (dy * ratio);
            this.dx = dx;
            this.dy = dy;
        }

        @Override
        public void run() {
            mScaleMatrix.postTranslate(dx_ratio, dy_ratio);
            temp_dx += dx_ratio;
            temp_dy += dy_ratio;
            if (dx_ratio == 0 && dy_ratio == 0) {
                isAutoTranslate = false;
                return;
            }
            checkBorderAndCenterWhenTranslate();
            setImageMatrix(mScaleMatrix);
            if (Math.abs(temp_dx) <= Math.abs(dx) && Math.abs(temp_dy) <= Math.abs(dy)) {
                postDelayed(this, 10);
                // Log.i("_dd", "temp_dx = " + temp_dx + "  temp_dy = " + temp_dy);
            } else {
                int end_dx = temp_dx - dx;
                int end_dy = temp_dy - dy;
                mScaleMatrix.postTranslate(end_dx, end_dy);
                checkBorderAndCenterWhenTranslate();
                setImageMatrix(mScaleMatrix);
                isAutoTranslate = false;
                // Log.i("_dd", "finish translating");
            }
        }
    }

    private class AutoScaleRunnable implements Runnable {
        private float mTargetScale;
        private float centerX;
        private float centerY;
        private final float BIGGER = 1.07F;
        private final float SMALLER = 0.93F;
        private float tempScale;

        public AutoScaleRunnable(float mTargetScale, float centerX, float centerY) {
            super();
            this.mTargetScale = mTargetScale;
            this.centerX = centerX;
            this.centerY = centerY;
            if (getCurrentScale() < mTargetScale) {
                tempScale = BIGGER;
            } else if (getCurrentScale() == mTargetScale) {
                tempScale = 1;
            } else {
                tempScale = SMALLER;
            }
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(tempScale, tempScale, centerX, centerY);
            checkBorderAndCenterWhenPostScale();
            setImageMatrix(mScaleMatrix);
            float currentScale = getCurrentScale();
            if ((tempScale > 1.0f && currentScale < mTargetScale)
                    | (tempScale < 1.0f && currentScale > mTargetScale)) {
                postDelayed(this, 30);
            } else {
                float scale = mTargetScale / currentScale;
                mScaleMatrix.postScale(scale, scale, centerX, centerY);
                checkBorderAndCenterWhenPostScale();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }
        }

    }

    private float getCurrentScale() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    private float getCurrentTranslateX() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MTRANS_X];
    }

    private float getCurrentTranslateY() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MTRANS_Y];
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        //scaleFactor > 1.0f表示正在放大
        float scaleFactor = detector.getScaleFactor();
        float currentScale = getCurrentScale();
        if (getDrawable() == null)
            return true;
        if ((currentScale < mMaxScale && scaleFactor > 1.0f)
                || (currentScale > mInitScale && scaleFactor < 1.0f)) {
            if (scaleFactor * currentScale < mInitScale) {
                scaleFactor = mInitScale / currentScale;
            }
            if (scaleFactor * currentScale > mMaxScale) {
                scaleFactor = mMaxScale / currentScale;
            }
            // 缩放过程中图片会移位
            mScaleMatrix.postScale(scaleFactor, scaleFactor,
                    detector.getFocusX(), detector.getFocusY());
            checkBorderAndCenterWhenPostScale();
            setImageMatrix(mScaleMatrix);
        }
        return true;
    }


    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    private void checkBorderAndCenterWhenPostScale() {
        RectF rectF = getMatrixRectF();
        float deltaX = 0.f;// δ
        float deltaY = 0.f;
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        // 图片大于控件时，修正偏移，防止白边
        if (rectF.width() >= viewWidth) {
            if (rectF.left > 0) {
                deltaX = -rectF.left;
            }
            if (rectF.right < viewWidth) {
                deltaX = viewWidth - rectF.right;
            }
        }
        if (rectF.height() >= viewHeight) {
            if (rectF.top > 0) {
                deltaY = -rectF.top;
            }
            if (rectF.bottom < viewHeight) {
                deltaY = viewHeight - rectF.bottom;
            }
        }
        // 图片小于控件时,居中显示
        if (rectF.width() < viewWidth) {
            deltaX = viewWidth / 2f - rectF.right + rectF.width() / 2f;
        }
        if (rectF.height() < viewHeight) {
            deltaY = viewHeight / 2f - rectF.bottom + rectF.height() / 2f;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        if (mGestureDetector.onTouchEvent(event))
            return true;
        float touchCenterX = 0f;
        float touchCenterY = 0f;
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            touchCenterX += event.getX(i);
            touchCenterY += event.getY(i);
        }
        touchCenterX /= pointerCount;
        touchCenterY /= pointerCount;
        if (mLastPointerCount != pointerCount) {
            isCanDrag = false;
            mLastCenterX = touchCenterX;
            mLastCenterY = touchCenterY;
        }
        mLastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rectF.width() > getWidth() + 0.01
                        || rectF.height() > getHeight() + 0.01) {
                    // 不允许父控件拦截该view的事件,+ 0.01是防止浮点操作的误差造成逻辑的判断失误
                    if (getParent() instanceof ViewPager)
                        getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (rectF.width() > getWidth() + 0.01
                        || rectF.height() > getHeight() + 0.01) {
                    if (getParent() instanceof ViewPager)
                        getParent().requestDisallowInterceptTouchEvent(true);
                }
                float dx = touchCenterX - mLastCenterX;
                float dy = touchCenterY - mLastCenterY;
                if (!isCanDrag) {
                    isCanDrag = isMoveValid(dx, dy);
                }
                if (isCanDrag) {
                    if (getDrawable() != null) {
                        isCheckedLeftAndRight = isCheckedTopAndBottom = true;
                        if (rectF.width() < getWidth()) {
                            isCheckedLeftAndRight = false;
                            dx = 0;
                        }
                        if (rectF.height() < getHeight()) {
                            isCheckedTopAndBottom = false;
                            dy = 0;
                        }
                        mScaleMatrix.postTranslate(dx, dy);
                        //Log.i("Translate", "Tx = " + getCurrentTranslateX());
                        //Log.i("Translate", "Ty = " + getCurrentTranslateY());
                        checkBorderAndCenterWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastCenterX = touchCenterX;
                mLastCenterY = touchCenterY;
                break;
            case MotionEvent.ACTION_UP:
                mLastPointerCount = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                break;

        }
        return true;
    }


    private void checkBorderAndCenterWhenTranslate() {
        RectF rectF = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (rectF.top > 0 && isCheckedTopAndBottom) {
            deltaY = -rectF.top;
        }
        if (rectF.bottom < viewHeight && isCheckedTopAndBottom) {
            deltaY = viewHeight - rectF.bottom;
        }
        if (rectF.left > 0 && isCheckedLeftAndRight) {
            deltaX = -rectF.left;
        }
        if (rectF.right < viewWidth && isCheckedLeftAndRight) {
            deltaX = viewWidth - rectF.right;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    private RectF getMatrixRectF() {
        //  Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            //获得原始图片的宽高
            rectF.set(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            //获得缩放后的宽高
            mScaleMatrix.mapRect(rectF);
        }
        return rectF;
    }

    private boolean isMoveValid(float dx, float dy) {

        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        System.out.println("onAttachedToWindow~~~~~~~~~~~~~");
        //在oncreate中View.getWidth和View.getHeight无法获得一个view的高度和宽度，这是因为View组件布局要在onResume回调后完成。所以现在需要使用getViewTreeObserver().addOnGlobalLayoutListener()来获得宽度或者高度
        //implements 该ViewTreeObserver.OnGlobalLayoutListener接口，实现onGlobalLayout方法
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        if (!mOnce) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            int imageWith = drawable.getIntrinsicWidth();
            int imageHeight = drawable.getIntrinsicHeight();
            float scale = 1.0f;
            if (imageWith >= viewWidth && imageHeight <= viewHeight) {
                scale = viewWidth * 1.0f / imageWith;
            }
            if (imageHeight >= viewHeight && imageWith <= viewWidth) {
                scale = viewHeight * 1.0f / imageHeight;
            }
            if ((imageWith >= viewWidth && imageHeight >= viewHeight)
                    || (imageWith < viewWidth && imageHeight < viewHeight)) {
                scale = Math.min(viewWidth * 1.0f / imageWith, viewHeight
                        * 1.0f / imageHeight);
            }
            mInitScale = scale;

            mMaxScale = mInitScale * mMaxScaleTimes;
            mDoubleClickScale = mInitScale * mDoubleClickScaleTimes;
            // 将图片移至当前控件的中心
            int dx = (viewWidth - imageWith) / 2;
            int dy = (viewHeight - imageHeight) / 2;
            // 平移
            mScaleMatrix.postTranslate(dx, dy);
            // 缩放
            mScaleMatrix.postScale(mInitScale, mInitScale, viewWidth / 2,
                    viewHeight / 2);
            setImageMatrix(mScaleMatrix);//设置mScaleMatrix为Image的Matrix

            mOnce = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }
}
