package com.yang.jigsaw.utils;

import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

/**
 * Created by YangHaiPing on 2016/2/19.
 */
public class ShowPopupWindow {
    public static void showPopupWindowOnTop(PopupWindow popupWindow,View view){
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //在上方显示
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,location[0],location[1] - popupWindow.getHeight());
    }
    public static void showPopupWindowDropDown(PopupWindow popupWindow,View view){
        //在下方显示
        popupWindow.showAsDropDown(view);
    }
    public static void showPopupWindowAtRight(PopupWindow popupWindow,View view){
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //在右边显示
        popupWindow.showAtLocation(view,Gravity.NO_GRAVITY,location[0] + popupWindow.getWidth(),location[1]);
    }
    public static void showPopupWindowAtLeft(PopupWindow popupWindow,View view){
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //在左边显示
        popupWindow.showAtLocation(view,Gravity.NO_GRAVITY,location[0] - popupWindow.getWidth(),location[1]);
    }
}
