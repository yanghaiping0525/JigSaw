package com.yang.jigsaw.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yang.jigsaw.R;

import java.util.List;

import com.yang.jigsaw.bean.FolderBean;
import com.yang.jigsaw.utils.ImageLoader;

/**
 * Created by Administrator on 2016/1/30.
 */
public class DirListAdapter extends ArrayAdapter<FolderBean> {
    private LayoutInflater mInflater;

    public DirListAdapter(Context context, List<FolderBean> dirsList) {
        super(context, 0, dirsList);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.popup_item, parent,false);
            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) convertView.findViewById(R.id.popup_imageView);
            viewHolder.dirName = (TextView) convertView.findViewById(R.id.popup_itemName);
            viewHolder.dirCount = (TextView) convertView.findViewById(R.id.popup_itemCount);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        FolderBean folderBean = getItem(position);
        viewHolder.image.setImageResource(R.mipmap.pictures_no);
        ImageLoader.getInstance().loadImageFromPath(folderBean.getFirstImgPath(), viewHolder.image);
        viewHolder.dirName.setText(folderBean.getName());
        viewHolder.dirCount.setText(folderBean.getImageCount() + "张");
        return convertView;
    }
    private class ViewHolder{
        ImageView image;
        TextView dirName;
        TextView dirCount;

    }
}
