package com.yang.jigsaw.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.yang.jigsaw.R;
import com.yang.jigsaw.bean.FileInfo;
import com.yang.jigsaw.constant.NetworkImageResource;
import com.yang.jigsaw.service.DownLoadService;
import com.yang.jigsaw.utils.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by YangHaiPing on 2016/3/7.
 */
public class NetworkImageDownLoadActivity extends AppCompatActivity {
    //底部功能栏
    private RelativeLayout mBottom, mDivider;
    private ViewPager mViewPager;
    //返回、下载按钮
    private ImageView mBack, mDownLoad;
    //底部功能栏是否隐藏标志
    private boolean isHide = false;
    private boolean isEnable = false;
    //触摸事件中触摸点的x、y坐标值
    private float xDown, yDown;
    //图片资源
    private List<String> mUrl = new ArrayList<>();
    //当前图片url
    private String mCurrentUrl;
    //当前图片位置
    private int mCurrentPosition;
    //最开始的位置
    private int mInitPosition;
    //最开始的图片bitmap
    private Bitmap mInitBitmap;
    private int mIndex;
    //文件下载目录
    private String fileDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_down_load);
        //初始化资源
        initResource();
        //初始化控件
        initFindViewById();
        //初始化事件
        initEvent();
    }

    private void initEvent() {
        //返回按钮事件
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkImageDownLoadActivity.this.finish();
            }
        });

        //设置触摸事件实现点击该View隐藏或显示功能栏,左右滑动只是翻页
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isEnable = true;
                        xDown = event.getX();
                        yDown = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - xDown) > 10 || Math.abs(event.getY() - yDown) > 10) {
                            isEnable = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isEnable) {
                            isHide = !isHide;
                            if (isHide) {
                                mBottom.setVisibility(View.INVISIBLE);
                                mDivider.setVisibility(View.INVISIBLE);
                            } else {
                                mBottom.setVisibility(View.VISIBLE);
                                mDivider.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                }
                return false;
            }
        });

        //拦截触摸事件防止响应下层mViewPager的触摸事件
        mBottom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        ImageAdapter adapter = new ImageAdapter(this, new ImageListener() {
            @Override
            public void displayImage(String url, ImageView imageView, int position) {
                ImageLoader.getInstance().loadImageFromUrl(url, imageView);
            }
        });
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mInitPosition);
        //增加缓存个数(默认缓存3个)
        // mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                mCurrentUrl = mUrl.get(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mDownLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FileInfo fileInfo = new FileInfo(mCurrentPosition, mCurrentUrl, 0, 0);
                File file = new File(fileDir, fileInfo.getName());
                if (file.exists()) {
                    Toast.makeText(NetworkImageDownLoadActivity.this, "该图片已经存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(NetworkImageDownLoadActivity.this, DownLoadService.class);
                intent.putExtra("fileInfo", fileInfo);
                intent.setAction(DownLoadService.ACTION_START);
                startService(intent);
            }
        });
    }

    private void initFindViewById() {
        mBottom = (RelativeLayout) findViewById(R.id.id_downLoad_bottom_container);
        mViewPager = (ViewPager) findViewById(R.id.id_downLoad_viewPager);
        mBack = (ImageView) findViewById(R.id.id_downLoad_back);
        mDivider = (RelativeLayout) findViewById(R.id.id_downLoad_divider);
        mDownLoad = (ImageView) findViewById(R.id.id_downLoad_downLoad);
    }

    private void initResource() {
        fileDir = NetworkImageDownLoadActivity.this.getFilesDir().getAbsolutePath();
        mInitPosition = getIntent().getIntExtra("position", 0);
        mInitBitmap = getIntent().getParcelableExtra("bitmap");
        mIndex = getIntent().getIntExtra("index", 0);
        mUrl = NetworkImageResource.getUrlFromIndex(mIndex);
        mCurrentUrl = mUrl.get(mInitPosition);
    }


    private class ImageAdapter extends PagerAdapter {
        private Context mContext;
        private ImageListener mListener;

        public ImageAdapter(Context context, ImageListener listener) {
            mContext = context;
            mListener = listener;
        }

        @Override
        public int getCount() {
            return mUrl.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            String url = mUrl.get(position);
            ImageView imageView;
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            container.addView(imageView);
            if (position == mInitPosition) {
                imageView.setImageBitmap(mInitBitmap);
            } else {
                imageView.setImageResource(R.mipmap.pictures_no);
            }
            mListener.displayImage(url, imageView, position);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            ImageView imageView = (ImageView) object;
//            container.removeView(imageView);
        }
    }

    public interface ImageListener {
        void displayImage(String url, ImageView imageView, int position);
    }
}
