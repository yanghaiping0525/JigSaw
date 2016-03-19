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
import com.yang.jigsaw.utils.ImageLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by YangHaiPing on 2016/2/5.
 */
public class MyPhotoAdapter extends BaseAdapter {
    //图片资源路径
    private String[] paths;
    private LayoutInflater mInflater;
    private Set<String> mContainer = new HashSet();
    //监听图片点击的接口
    private OnPhotoSelectListener mPhotoSelectListener;
    //监听添加按钮点击的接口
    private OnAddIconClickListener mAddIconClickListener;
    //添加按钮所在的View
    private View mAddView;

    public interface OnPhotoSelectListener {
        void photoSelect(Set<String> paths);
    }

    public void setOnPhotoSelectListener(OnPhotoSelectListener listener) {
        this.mPhotoSelectListener = listener;
    }

    public interface OnAddIconClickListener {
        void addPicture();
    }

    public void setOnAddIconClickListener(OnAddIconClickListener mAddIconClickListener) {
        this.mAddIconClickListener = mAddIconClickListener;
    }

    public MyPhotoAdapter(Context context, String[] paths) {
        this.paths = paths;
        mInflater = LayoutInflater.from(context);
        mAddView = mInflater.inflate(R.layout.add_photo_gradview, null);
        //添加"添加按钮"的点击事件
        mAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddIconClickListener != null) {
                    mAddIconClickListener.addPicture();
                }
            }
        });
    }

    @Override
    public int getCount() {
        //加1是由于添加"添加按钮"
        if (paths != null && paths.length > 0) {
            return paths.length + 1;
        } else
            return 1;
    }

    @Override
    public Object getItem(int position) {
        if (paths != null && paths.length > 0 && position < getCount() - 1) {
            return paths[position];
        } else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //如果不是"添加按钮"的位置
        if (position < getCount() - 1) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.gridview_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.item_imageView);
                viewHolder.selector = (ImageButton) convertView.findViewById(R.id.item_selector);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                if (viewHolder == null) {
                    convertView = mInflater.inflate(R.layout.gridview_item, null, false);
                    final ViewHolder viewHolder_ = new ViewHolder();
                    viewHolder_.imageView = (ImageView) convertView.findViewById(R.id.item_imageView);
                    viewHolder_.selector = (ImageButton) convertView.findViewById(R.id.item_selector);
                    //convertView.setTag(viewHolder);
                    //先将图片重置
                    viewHolder_.imageView.setImageResource(R.mipmap.pictures_no);
                    viewHolder_.imageView.setColorFilter(null);
                    viewHolder_.selector.setVisibility(View.INVISIBLE);
                    if (paths != null && paths.length > 0) {
                        final String filePath = paths[position];
                        //根据文件路径加载图片
                        ImageLoader.getInstance().loadImageFromPath(filePath, viewHolder_.imageView);
                        viewHolder_.imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mContainer.contains(filePath)) {
                                    mContainer.remove(filePath);
                                    viewHolder_.imageView.setColorFilter(null);
                                    viewHolder_.selector.setVisibility(View.INVISIBLE);
                                } else {
                                    mContainer.add(filePath);
                                    viewHolder_.imageView.setColorFilter(Color.parseColor("#77000000"));
                                    viewHolder_.selector.setVisibility(View.VISIBLE);
                                }
                                if (mPhotoSelectListener != null) {
                                    mPhotoSelectListener.photoSelect(mContainer);
                                }
                            }
                        });
                    }
                }
            }
            if (viewHolder != null) {
                //先将图片重置
                viewHolder.imageView.setImageResource(R.mipmap.pictures_no);
                viewHolder.imageView.setColorFilter(null);
                viewHolder.selector.setVisibility(View.INVISIBLE);
                if (paths != null && paths.length > 0) {
                        final String filePath = paths[position];
                        //根据文件路径加载图片
                        ImageLoader.getInstance().loadImageFromPath(filePath, viewHolder.imageView);
                        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mContainer.contains(filePath)) {
                                    mContainer.remove(filePath);
                                    viewHolder.imageView.setColorFilter(null);
                                    viewHolder.selector.setVisibility(View.INVISIBLE);
                                } else {
                                    mContainer.add(filePath);
                                    viewHolder.imageView.setColorFilter(Color.parseColor("#77000000"));
                                    viewHolder.selector.setVisibility(View.VISIBLE);
                                }
                                if (mPhotoSelectListener != null) {
                                    mPhotoSelectListener.photoSelect(mContainer);
                                }
                            }
                        });
                }
            }
            return convertView;
        }
        //添加按钮的位置
        else {
            return mAddView;
        }

    }

    class ViewHolder {
        private ImageView imageView;
        private ImageButton selector;
    }
}
