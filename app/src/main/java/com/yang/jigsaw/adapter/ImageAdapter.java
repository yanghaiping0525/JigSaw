package com.yang.jigsaw.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.yang.jigsaw.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.yang.jigsaw.utils.ImageLoader;

/**
 * Created by YangHaiPing on 2016/1/30.
 */
public class ImageAdapter extends BaseAdapter {
    //图片的名称
    private List<String> mImageNames;
    //图片所在的文件夹路径
    private String mDirPath;
    private LayoutInflater mInflater;
    //已选择的图片集合
    private Set<String> mSelectedPics = new HashSet<>();//如果这里使用Static下一次加载状态还是显示上次已选择的状态
    //图片长按事件接口(作为拓展,没有使用)
    private OnImageLongClickListener mLongClickListener;
    //图片的点击事件接口
    private OnImageChoiceListener mImageChoiceListener;

    public interface OnImageLongClickListener {
        void longClick(Set<String> paths);
    }


    public interface OnImageChoiceListener {
        void imageChoice(Set<String> paths);
    }

    public void setOnImageLongClickListener(OnImageLongClickListener listener) {
        this.mLongClickListener = listener;
    }


    public void setOnImageChoiceListener(OnImageChoiceListener listener) {
        this.mImageChoiceListener = listener;
    }

    public ImageAdapter(Context context, List<String> picsPaths, String dirPath) {
        this.mImageNames = picsPaths;
        this.mDirPath = dirPath;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mImageNames.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.gridview_item, parent,
                    false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView
                    .findViewById(R.id.item_imageView);
            viewHolder.selector = (ImageButton) convertView
                    .findViewById(R.id.item_selector);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //重置状态
        viewHolder.imageView.setImageResource(R.mipmap.pictures_no);
        viewHolder.selector.setVisibility(View.INVISIBLE);
        viewHolder.imageView.setColorFilter(null);
        final String filePath = mDirPath + "/" + mImageNames.get(position);
        ImageLoader.getInstance().loadImageFromPath(filePath, viewHolder.imageView);
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectedPics.contains(filePath)) {
                    mSelectedPics.remove(filePath);
                    viewHolder.imageView.setColorFilter(null);
                    viewHolder.selector.setVisibility(View.INVISIBLE);
                } else {
                    mSelectedPics.add(filePath);
                    viewHolder.imageView.setColorFilter(Color.parseColor("#77000000"));
                    viewHolder.selector.setVisibility(View.VISIBLE);
                }
                if (mImageChoiceListener != null) {
                    mImageChoiceListener.imageChoice(mSelectedPics);
                }
            }
        });
        viewHolder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongClickListener != null) {
                    mLongClickListener.longClick(mSelectedPics);
                }
                return false;
            }
        });

        if (mSelectedPics.contains(filePath)) {
            viewHolder.imageView.setColorFilter(Color.parseColor("#77000000"));
            viewHolder.selector.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private class ViewHolder {
        private ImageView imageView;
        private ImageButton selector;
    }
}
