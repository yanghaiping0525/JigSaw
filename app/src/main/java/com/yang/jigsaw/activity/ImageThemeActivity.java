package com.yang.jigsaw.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.yang.jigsaw.R;
import com.yang.jigsaw.adapter.NetworkImageAdapter;
import com.yang.jigsaw.constant.NetworkImageResource;

import java.util.List;

/**
 * Created by YangHaiPing on 2016/3/8.
 */
public class ImageThemeActivity extends Activity {
    //返回按钮
    private ImageView mBack;
    //显示相应主题图片
    private GridView mGridView;
    //图片资源
    private List<String> mUrl;
    private int mIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_theme);
        mBack = (ImageView) findViewById(R.id.id_back);
        mGridView = (GridView) findViewById(R.id.id_gridView);
        mIndex = getIntent().getIntExtra("index", 0);
        //获得相应的主题图片
        mUrl = NetworkImageResource.getThumbUrlFromIndex(mIndex);
        //返回按钮点击事件
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageThemeActivity.this.finish();
            }
        });
        NetworkImageAdapter adapter = new NetworkImageAdapter(this, mUrl);
        adapter.setOnImageClickListener(new NetworkImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position, Bitmap bitmap) {
                Intent intent = new Intent(ImageThemeActivity.this, NetworkImageDownLoadActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("bitmap", bitmap);
                intent.putExtra("index", mIndex);
                startActivity(intent);
            }
        });
        //显示相应主题图片
        mGridView.setAdapter(adapter);
    }
}
