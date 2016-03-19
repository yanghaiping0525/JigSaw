package com.yang.jigsaw.camera.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yang.jigsaw.R;

import java.util.List;

/**
 * Created by Administrator on 2016/2/13.
 */
public class MyEffectSelectedAdapter {
    private LayoutInflater mInflater;
    private List<Integer> mIconImageResource;
    private List<String> mIconNames;
    private int mClickPosition;

    public MyEffectSelectedAdapter(Context context, List<Integer> resourceIds, List<String> iconNames) {
        mIconImageResource = resourceIds;
        mIconNames = iconNames;
        mInflater = LayoutInflater.from(context);
    }


    public int getCount() {
        return mIconImageResource.size();
    }


    public Object getItem(int position) {
        return mIconImageResource.get(position);
    }


    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.gallery_item, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.id_effect_icon);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.id_effect_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imageView.setImageResource(mIconImageResource.get(position));
        if (position == mClickPosition) {
            convertView.setBackgroundColor(Color.parseColor("#AA024DA4"));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        viewHolder.textView.setText(mIconNames.get(position));
        return convertView;
    }

    public void recordClickPosition(int position) {
        if (position < getCount() && position >= 0) {
            mClickPosition = position;
        }
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
