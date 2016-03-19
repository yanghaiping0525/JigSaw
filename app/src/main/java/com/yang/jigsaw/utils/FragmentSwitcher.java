package com.yang.jigsaw.utils;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

/**
 * Created by Administrator on 2016/2/17.
 */
public class FragmentSwitcher {
    public static void turnToFragment(FragmentManager fragmentManager, Class<? extends Fragment> fromFragmentClass, Class<? extends Fragment> toFragmentClass, Bundle args, int contentId) {
        //被切换的Fragment标签
        String fromTag = fromFragmentClass.getSimpleName();
        //切换到的Fragment标签
        String toTag = toFragmentClass.getSimpleName();
        //查找切换的Fragment
        Fragment fromFragment = fragmentManager.findFragmentByTag(fromTag);
        Fragment toFragment = fragmentManager.findFragmentByTag(toTag);
        //如果要切换到的Fragment不存在，则创建
        if (toFragment == null) {
            try {
                toFragment = toFragmentClass.newInstance();
                toFragment.setArguments(args);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        //如果有参数传递
        if (args != null && !args.isEmpty()) {
            toFragment.getArguments().putAll(args);
        }
        //Fragment事务
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //设置Fragment切换效果
        //fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        /**
         * 如果要切换到的Fragment没有被Fragment事务添加，则隐藏被切换的Fragment，添加要切换的Fragment
         * 否则，则隐藏被切换的Fragment，显示要切换的Fragment
         */
        if (!toFragment.isAdded()) {
            if (fromFragment != null && fragmentManager.findFragmentByTag(fromFragment.getTag()) != null) {
                fragmentTransaction.hide(fromFragment).add(contentId, toFragment, toTag);
            } else {
                throw new RuntimeException("被替换对象为空");
            }
        } else {
            if (fromFragment != null && fragmentManager.findFragmentByTag(fromFragment.getTag()) != null) {
                fragmentTransaction.hide(fromFragment).show(toFragment);
            } else {
                throw new RuntimeException("被替换对象为空");
            }
        }
        //添加到返回堆栈
        //mFragmentTransaction.addToBackStack(toTag);
        //不保留状态提交事务
        // mFragmentTransaction.commitAllowingStateLoss();
        fragmentTransaction.commit();
    }
}
