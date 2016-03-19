package com.yang.jigsaw.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by Administrator on 2016/2/20.
 */
public class ActionBarHelper {
    public static void showCustomActionBar(ActionBar actionBar, View customView) {
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setBackgroundDrawable(null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        Toolbar parent = (Toolbar) customView.getParent();
        //v7包需要手动设置view填满actionbar左右两边，布局中最外层容器height必须为wrap_content
        parent.setContentInsetsAbsolute(0, 0);
    }

    public static float getActionBarHeight(Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        return typedArray.getDimension(0, 0);
    }
}
