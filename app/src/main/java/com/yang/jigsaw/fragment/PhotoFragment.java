package com.yang.jigsaw.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.yang.jigsaw.R;
import com.yang.jigsaw.utils.FragmentSwitcher;

/**
 * Created by YangHaiPing on 2016/2/16.
 */
public class PhotoFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {
    private RadioGroup mRadioGroup;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    //自定义图片Fragment
    private MyPhotoFragment myPhotoFragment;
    //默认图片Fragment
    private DefaultPhotoFragment defaultPhotoFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_fragment, container, false);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.id_radio_group_photo);
        mRadioGroup.setOnCheckedChangeListener(this);
        mFragmentManager = getChildFragmentManager();
        //先显示自定义图片Fragment
        myPhotoFragment = new MyPhotoFragment();
        defaultPhotoFragment = new DefaultPhotoFragment();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.id_fragment_photo_content, myPhotoFragment, myPhotoFragment.getClass().getSimpleName());
        mFragmentTransaction.commit();
        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            //两个Fragment之间的替换和隐藏
            case R.id.id_fragment_myPhoto:
                FragmentSwitcher.turnToFragment(mFragmentManager, defaultPhotoFragment.getClass(), myPhotoFragment.getClass(), null, R.id.id_fragment_photo_content);
                break;
            case R.id.id_fragment_defaultPhoto:
                FragmentSwitcher.turnToFragment(mFragmentManager, myPhotoFragment.getClass(), defaultPhotoFragment.getClass(), null, R.id.id_fragment_photo_content);
                break;
        }
    }
}
