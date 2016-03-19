package com.yang.jigsaw.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yang.jigsaw.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/7.
 */
public class ImageCycleView extends LinearLayout {
    private Context mContext;
    private ViewPager mViewPager;
    private ImageCycleAdapter mPageAdapter;
    private ViewGroup mGroup;
    private ImageView mImageView;
    private ImageView[] mIndicators;
    private int mImageIndex = 0;
    private float mScale;
    private boolean isStop;
    private TextView mImageName;
    private List<String> mImageDescription = new ArrayList<>();
    private Handler mHandler = new Handler();
    private int mCycleTime = 3000;

    public void setCycleTime(int cycleTime) {
        this.mCycleTime = cycleTime;
    }


    public ImageCycleView(Context context) {
        this(context, null);
    }

    public ImageCycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ImageCycleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mScale = context.getResources().getDisplayMetrics().density;
        LayoutInflater.from(context).inflate(R.layout.cycle_view, this);
        mViewPager = (ViewPager) findViewById(R.id.adv_pager);
        mViewPager.addOnPageChangeListener(new GuidePageChangeListener());
        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        startImageTimerTask();
                        break;
                    default:
                        stopImageTimerTask();
                        break;
                }
                return false;
            }
        });
        // 滚动图片右下指示器视
        mGroup = (ViewGroup) findViewById(R.id.circles);
        mImageName = (TextView) findViewById(R.id.viewGroup2);
    }

    public void setImageResource(List<String> description, List<String> url, ImageCycleViewListener listener) {
        mImageDescription = description;
        if (url != null && url.size() > 0) {
            this.setVisibility(View.VISIBLE);
        } else {
            this.setVisibility(View.GONE);
            return;
        }
        mGroup.removeAllViews();
        final int imageCount = url.size();
        mIndicators = new ImageView[imageCount];
        for (int i = 0; i < imageCount; i++) {
            ImageView imageView = new ImageView(mContext);
            int imageParams = (int) (mScale * 10 + 0.5f);// XP与DP转换，适应应不同分辨率
            int imagePadding = (int) (mScale * 5 + 0.5f);
            LayoutParams params = new LayoutParams(imageParams, imageParams);
            params.rightMargin = 10;
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(params);
            imageView.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
            mIndicators[i] = imageView;
            if (i == 0) {
                mIndicators[i].setBackgroundResource(R.mipmap.banner_dot_focus);
            } else {
                mIndicators[i].setBackgroundResource(R.mipmap.banner_dot_normal);
            }
            mGroup.addView(mIndicators[i]);
        }

        mImageName.setText(mImageDescription.get(0));
        mPageAdapter = new ImageCycleAdapter(mContext, url, mImageDescription, listener);
        mViewPager.setAdapter(mPageAdapter);
        startImageTimerTask();
    }


    public void startImageTimerTask() {
        stopImageTimerTask();
        mHandler.postDelayed(mImageTimeTask, mCycleTime);
    }

    private void stopImageTimerTask() {
        isStop = true;
        mHandler.removeCallbacks(mImageTimeTask);
    }

    private Runnable mImageTimeTask = new Runnable() {
        @Override
        public void run() {
            if (mIndicators != null) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                if (!isStop) {
                    mHandler.postDelayed(mImageTimeTask, mCycleTime);
                }
            }
        }
    };


    private final class GuidePageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            position = position % mIndicators.length;
            mImageIndex = position;
            mIndicators[position].setBackgroundResource(R.mipmap.banner_dot_focus);
            mImageName.setText(mImageDescription.get(position));
            for (int i = 0; i < mIndicators.length; i++) {
                if (i != position) {
                    mIndicators[i].setBackgroundResource(R.mipmap.banner_dot_normal);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE)
                startImageTimerTask();
        }

    }


    private class ImageCycleAdapter extends PagerAdapter {
        private List<ImageView> mImageViewCache = new ArrayList<>();
        private List<String> mImageUrl = new ArrayList<>();
        private ImageCycleViewListener mListener;
        private Context mContext;

        public ImageCycleAdapter(Context context, List<String> url, List<String> names, ImageCycleViewListener listener) {
            this.mContext = context;
            this.mImageUrl = url;
            mImageDescription = names;
            mListener = listener;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            String url = mImageUrl.get(position % mImageUrl.size());
            ImageView imageView;
            if (mImageViewCache.isEmpty()) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = mImageViewCache.remove(0);
            }
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onImageClick(position % mImageUrl.size(), v);
                }
            });
            imageView.setTag(url);
            container.addView(imageView);
            mListener.displayImage(url, imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView imageView = (ImageView) object;
            mViewPager.removeView(imageView);
            mImageViewCache.add(imageView);
        }
    }

    public interface ImageCycleViewListener {
        void displayImage(String url, ImageView imageView);

        void onImageClick(int position, View imageView);
    }

}
