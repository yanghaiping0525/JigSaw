package com.yang.jigsaw.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yang.jigsaw.R;
import com.yang.jigsaw.utils.ImageLoader;

import java.util.List;

/**
 * Created by YangHaiPing on 2016/3/7.
 */
public class NetworkImageAdapter extends BaseAdapter {
    private List<String> mImageUrl;
    private LayoutInflater mInflater;
    private OnImageClickListener mListener;

    public interface OnImageClickListener {
        void onImageClick(int position, Bitmap bitmap);
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.mListener = listener;
    }

    public NetworkImageAdapter(Context context, List<String> imageUrl) {
        this.mImageUrl = imageUrl;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mImageUrl.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageUrl.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.grad_view_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) convertView.findViewById(R.id.id_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.image.setImageResource(R.mipmap.pictures_no);
        ImageLoader.getInstance(9, ImageLoader.QueueType.FIFO).loadImageFromUrl(mImageUrl.get(position), viewHolder.image);
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    viewHolder.image.setDrawingCacheEnabled(true);
                    //获得该图片的bitmap对象
                    Bitmap bitmap = viewHolder.image.getDrawingCache();
                    mListener.onImageClick(position, bitmap);
                }
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView image;
    }
}
