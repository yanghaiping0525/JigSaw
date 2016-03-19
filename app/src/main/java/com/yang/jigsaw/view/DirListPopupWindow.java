package com.yang.jigsaw.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.yang.jigsaw.R;

import java.util.List;

import com.yang.jigsaw.adapter.DirListAdapter;
import com.yang.jigsaw.bean.FolderBean;

/**
 * Created by YangHaiPing on 2016/1/30.
 */
public class DirListPopupWindow extends PopupWindow{
    private int mWidth;
    private int mHeight;
    private View mConvertView;
    private ListView mDirList;
    public OnDirSelectedListener mListener;
    public interface OnDirSelectedListener{
        void onSelected(FolderBean folderBean, int position);
    }
    public void setOnDirSelectedListener(OnDirSelectedListener listener) {
        this.mListener = listener;
    }
    public DirListPopupWindow(final List<FolderBean> dirs, Context context) {
        mConvertView = LayoutInflater.from(context).inflate(R.layout.popup_layout, null);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mWidth = displayMetrics.widthPixels;
        mHeight = (int) (displayMetrics.heightPixels * 0.7);
        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        mDirList = (ListView) mConvertView.findViewById(R.id.popup_listView);
        mDirList.setAdapter(new DirListAdapter(context, dirs));
        //设置listView的点击事件
        mDirList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mListener != null) {
                    mListener.onSelected(dirs.get(position),position);
                }
            }
        });

    }

}
