package com.yang.jigsaw.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.yang.jigsaw.R;

/**
 * Created by YangHaiPing on 2016/2/16.
 */
public class DefaultPhotoFragment extends Fragment{
    private LayoutInflater mInflater;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_photo_fragment,container,false);
        mInflater = inflater;
        GridView gridView = (GridView) view.findViewById(R.id.id_gridView_default_photos);
        //默认图片的资源
        final Integer[] resIds = new Integer[]{R.mipmap.bg_1,R.mipmap.bg_2,R.mipmap.bg_3,R.mipmap.bg_4};
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return resIds.length;
            }

            @Override
            public Object getItem(int position) {
                return resIds[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;
                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.default_photo_gradview,parent,false);
                    viewHolder = new ViewHolder();
                    viewHolder.imageView = (ImageView) convertView.findViewById(R.id.item_imageView_default_photo);
                    convertView.setTag(viewHolder);
                }else{
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                viewHolder.imageView.setImageResource(resIds[position]);
                return convertView;
            }
        };
        gridView.setAdapter(adapter);
        return view;
    }


    class ViewHolder{
        private ImageView imageView;
    }
}
