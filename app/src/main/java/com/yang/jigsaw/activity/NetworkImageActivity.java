package com.yang.jigsaw.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yang.jigsaw.R;
import com.yang.jigsaw.adapter.NetworkImageAdapter;
import com.yang.jigsaw.constant.NetworkImageResource;
import com.yang.jigsaw.utils.ImageLoader;
import com.yang.jigsaw.utils.SystemBarTintManager;
import com.yang.jigsaw.view.ImageCycleView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangHaiPing on 2016/3/7.
 */
public class NetworkImageActivity extends Activity {
    //默认图片显示区域
    private GridView mGridView;
    //默认图片资源
    private List<String> mImageUrl = new ArrayList<>();
    //滚动栏资源
    private List<String> mCycleUrl = new ArrayList<>();
    //滚动栏显示区域
    private ImageCycleView mImageCycleView;
    //返回按钮
    private ImageView mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image);
        //设置状态栏的颜色
        initStatusBarColor();
        //初始化控件
        initFindViewById();
        //初始化数据、设置点击事件
        initDataAndEvent();
    }

    private void initDataAndEvent() {
        //获得卡通图片缩略图资源
        mImageUrl = NetworkImageResource.getCartoonThumb();
        //获得滚动栏背景资源
        mCycleUrl = NetworkImageResource.getCycle();
        //获得滚动栏文字描述
        List<String> cycleNames = NetworkImageResource.getCycleTitle();
        //设置滚动栏布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getScreenHeight(this) * 3 / 10);
        mImageCycleView.setLayoutParams(params);
        //设置滚动栏内容切换时间间隔
        mImageCycleView.setCycleTime(5000);
        //设置滚动栏数据内容
        mImageCycleView.setImageResource(cycleNames, mCycleUrl, new ImageCycleView.ImageCycleViewListener() {
            @Override
            public void displayImage(String url, ImageView imageView) {
                ImageLoader.getInstance().loadImageFromUrl(url, imageView);
            }

            @Override
            public void onImageClick(int position, View imageView) {
                Intent intent = new Intent(NetworkImageActivity.this, ImageThemeActivity.class);
                intent.putExtra("index", (position + 1));
                startActivity(intent);
            }
        });
        //返回按钮点击事件
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkImageActivity.this.finish();
            }
        });
        NetworkImageAdapter adapter = new NetworkImageAdapter(this, mImageUrl);
        adapter.setOnImageClickListener(new NetworkImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position, Bitmap bitmap) {
                Intent intent = new Intent(NetworkImageActivity.this, NetworkImageDownLoadActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("bitmap", bitmap);
                intent.putExtra("index", 0);
                startActivity(intent);
            }
        });
        //显示卡通图片
        mGridView.setAdapter(adapter);
    }

    private void initFindViewById() {
        mGridView = (GridView) findViewById(R.id.id_gridView);
        mImageCycleView = (ImageCycleView) findViewById(R.id.id_cycleView);
        mBack = (ImageView) findViewById(R.id.id_back);
    }

    private void initStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.actionbar);//通知栏所需颜色
        }
    }

    public static int getScreenHeight(Context context) {
        if (null == context) {
            return 0;
        }
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}
