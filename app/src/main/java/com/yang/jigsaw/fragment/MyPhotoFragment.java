package com.yang.jigsaw.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.yang.jigsaw.R;
import com.yang.jigsaw.activity.ImageLoaderActivity;
import com.yang.jigsaw.activity.NetworkImageActivity;
import com.yang.jigsaw.adapter.MyPhotoAdapter;
import com.yang.jigsaw.camera.MyCamera;
import com.yang.jigsaw.utils.ScreenSize;
import com.yang.jigsaw.utils.ShowPopupWindow;
import com.yang.jigsaw.view.AlertDialogPopupWindow;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by YangHaiPing on 2016/2/16.
 */
public class MyPhotoFragment extends Fragment implements MyPhotoAdapter.OnPhotoSelectListener, MyPhotoAdapter.OnAddIconClickListener {
    //显示图片的容器
    private GridView mGridView;
    //图片的文件路径
    private String[] paths;
    //被选中的图片的集合
    private Set<String> mSelectedPaths = new HashSet<>();
    //自定义图片适配器
    private MyPhotoAdapter adapter;
    private static final int REQUEST_CODE = 0x100;
    private static final int RESULT_OK = 0x200;
    private LayoutInflater mInflater;
    //移除图片的图标
    private ImageButton mTrashCan;
    private static final String DOWNLOAD_FINISH = "downLoadFinish";
    //监听网络下载完成事件的广播接受者
    private BroadcastReceiver mDownLoadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DOWNLOAD_FINISH)) {
                loadPictures();
                adapter = new MyPhotoAdapter(getActivity(), paths);
                adapter.setOnPhotoSelectListener(MyPhotoFragment.this);
                adapter.setOnAddIconClickListener(MyPhotoFragment.this);
                mGridView.setAdapter(adapter);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.myphotos_fragment, container, false);
        mGridView = (GridView) view.findViewById(R.id.id_gridView_myPhoto);
        mInflater = inflater;
        mTrashCan = (ImageButton) getActivity().findViewById(R.id.id_actionbar_trash);
        if (mTrashCan != null) {
            mTrashCan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialogPopupWindow alertDialog = new AlertDialogPopupWindow(getActivity());
                    alertDialog.setPositiveButton("确定", new AlertDialogPopupWindow.OnClickListener() {
                        @Override
                        public void onClick() {
                            removePhotos(mSelectedPaths);
                            alertDialog.dismiss();
                            mTrashCan.setVisibility(View.INVISIBLE);
                        }
                    });
                    alertDialog.setNegativeButton("取消", new AlertDialogPopupWindow.OnClickListener() {
                        @Override
                        public void onClick() {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.setTitleIcon(R.mipmap.delete_icon_new);
                    alertDialog.setContent("将从游戏中移除该照片，但仍然保会留在手机中。\n(可以点击\" + \"重新添加)");
                    alertDialog.setTitle("注意！");
                    alertDialog.addPicture(R.mipmap.sleep);
                    alertDialog.enableMove();
                    alertDialog.show();
                }
            });
        }
        //加载自定义图片
        loadPictures();
        //显示自定义图片
        adapter = new MyPhotoAdapter(getActivity(), paths);
        adapter.setOnPhotoSelectListener(this);
        adapter.setOnAddIconClickListener(this);
        mGridView.setAdapter(adapter);
        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DOWNLOAD_FINISH);
        getActivity().registerReceiver(mDownLoadReceiver, intentFilter);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销广播监听者
        if (mDownLoadReceiver != null) {
            getActivity().unregisterReceiver(mDownLoadReceiver);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                loadPictures();
                adapter = new MyPhotoAdapter(getActivity(), paths);
                adapter.setOnPhotoSelectListener(this);
                adapter.setOnAddIconClickListener(this);
                mGridView.setAdapter(adapter);
            }
        }
    }

    //监听图片点击事件
    @Override
    public void photoSelect(Set<String> paths) {
        mSelectedPaths = paths;
        //如果有选中的图片,显示删除图标
        if (mSelectedPaths.size() > 0) {
            mTrashCan.setVisibility(View.VISIBLE);
        } else {
            mTrashCan.setVisibility(View.INVISIBLE);
        }
    }

    //监听添加按钮点击事件
    @Override
    public void addPicture() {
        //先弹出PopupWindow,在布局中有四个Button,可以选择打开自定义图库,网络下载,自定义相机,取消
        View view = mInflater.inflate(R.layout.add_picture_popup_window, null);
        Button photoSelectBtn = (Button) view.findViewById(R.id.id_popupWindow_select_photos);
        Button downLoadBtn = (Button) view.findViewById(R.id.id_popupWindow_downLoad);
        Button cameraBtn = (Button) view.findViewById(R.id.id_popupWindow_take_photos);
        Button cancelBtn = (Button) view.findViewById(R.id.id_popupWindow_cancel);
        final PopupWindow popupWindow = new PopupWindow(view);
        int width = ScreenSize.getWidth(getActivity());
        int height = ScreenSize.getHeight(getActivity());
        popupWindow.setHeight(height / 2);
        popupWindow.setWidth(width);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    popupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                params.alpha = 1.0f;
                getActivity().getWindow().setAttributes(params);
            }
        });
        //点击选择打开自定义图库
        photoSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRootFragment().startActivityForResult(new Intent(getActivity(), ImageLoaderActivity.class), REQUEST_CODE);
                popupWindow.dismiss();
            }
        });
        //点击选择跳转到网路下载界面
        downLoadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NetworkImageActivity.class));
                popupWindow.dismiss();
            }
        });
        //点击打开自定义相机
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRootFragment().startActivityForResult(new Intent(getActivity(), MyCamera.class), REQUEST_CODE);
                popupWindow.dismiss();
            }
        });
        //点击取消
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.setAnimationStyle(R.style.PopupWindow_AddPhoto_Animation);
        ShowPopupWindow.showPopupWindowDropDown(popupWindow, getActivity().findViewById(R.id.id_radio_group));
        WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
        params.alpha = 0.3f;
        getActivity().getWindow().setAttributes(params);
    }



    private void removePhotos(Set<String> mSelectedPaths) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("imagePaths", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> filePaths = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            filePaths = sharedPreferences.getStringSet("imagePaths", new HashSet<String>());
        }
        Iterator<String> iterator = mSelectedPaths.iterator();
        while (iterator.hasNext()) {
            String temp = iterator.next();
            if (filePaths != null && filePaths.contains(temp)) {
                filePaths.remove(temp);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor.clear();
            editor.commit();
            editor.putStringSet("imagePaths", filePaths);
            editor.commit();
        }
        sharedPreferences = getContext().getSharedPreferences("imagePaths", Context.MODE_PRIVATE);
        filePaths = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            filePaths = sharedPreferences.getStringSet("imagePaths", new HashSet<String>());
        }
        int count = filePaths.size();
        paths = new String[count];
        if (filePaths != null && count > 0) {
            Iterator<String> it = filePaths.iterator();
            paths = new String[filePaths.size()];
            for (int i = 0; i < count; i++) {
                paths[i] = it.next();
            }
        }
        adapter = new MyPhotoAdapter(getActivity(), paths);
        adapter.setOnPhotoSelectListener(this);
        adapter.setOnAddIconClickListener(this);
        mGridView.setAdapter(adapter);
    }


    private Fragment getRootFragment() {
        Fragment fragment = getParentFragment();
        while (fragment.getParentFragment() != null) {
            fragment = fragment.getParentFragment();
        }
        return fragment;
    }

    private void loadPictures() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("imagePaths", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> filePaths = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            filePaths = sharedPreferences.getStringSet("imagePaths", new HashSet<String>());
        }
        int count = filePaths.size();
        int finalCount = 0;
        if (filePaths != null && count > 0) {
            Iterator<String> iterator = filePaths.iterator();

            for (int i = 0; i < count; i++) {
                File file = new File(iterator.next());
                if (file.exists()) {
                    finalCount++;
                } else {
                    iterator.remove();
                }
            }
        }
        //如果有无效的图片,更新资源信息
        if (finalCount < count) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.clear();
                editor.commit();
                editor.putStringSet("imagePaths", filePaths);
                editor.commit();
            }
        }

        if (finalCount > 0) {
            Iterator<String> iterator = filePaths.iterator();
            paths = new String[finalCount];
            for (int i = 0; i < finalCount; i++) {
                paths[i] = iterator.next();
            }
        }
    }


}
