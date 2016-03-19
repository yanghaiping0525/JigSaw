package com.yang.jigsaw.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yang.jigsaw.R;
import com.yang.jigsaw.utils.ScreenSize;

/**
 * Created by Administrator on 2016/2/23.
 */
public class AlertDialogPopupWindow extends PopupWindow {
    private View parentView;
    private int mScreenWidth, mScreenHeight;
    private OnClickListener mPositiveListener, mNeutralListener, mNegativeListener;
    private Button mPositiveButton, mNeutralButton, mNegativeButton;
    private TextView mContent, mTitle;
    private RelativeLayout mTitleContainer;
    private RelativeLayout mTitleLine, mButtonLine_one, mButtonLine_Two;
    private ImageView mTitleIcon;
    private boolean isPositiveExist, isNegativeExist, isNeutralExist;
    private ImageView mPicture;
    private int lastX = 0, lastY = 0;
    private View mConvertView;
    public interface OnClickListener {
        void onClick();
    }


    public AlertDialogPopupWindow(Activity activity) {
        mConvertView = LayoutInflater.from(activity).inflate(R.layout.alert_dialog_popup_window, null);
        parentView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        mScreenHeight = ScreenSize.getHeight(activity);
        mScreenWidth = ScreenSize.getWidth(activity);
        setContentView(mConvertView);
        setWidth(mScreenWidth);
        //setHeight(mScreenHeight * 6 / 16);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        mPositiveButton = (Button) mConvertView.findViewById(R.id.id_alertDialog_ok);
        mNeutralButton = (Button) mConvertView.findViewById(R.id.id_alertDialog_other);
        mNegativeButton = (Button) mConvertView.findViewById(R.id.id_alertDialog_cancel);
        mTitle = (TextView) mConvertView.findViewById(R.id.id_alertDialog_title);
        mTitleIcon = (ImageView) mConvertView.findViewById(R.id.id_alertDialog_title_icon);
        mTitleContainer = (RelativeLayout) mConvertView.findViewById(R.id.id_alertDialog_title_container);
        mContent = (TextView) mConvertView.findViewById(R.id.id_alertDialog_content);
        mTitleLine = (RelativeLayout) mConvertView.findViewById(R.id.id_alertDialog_title_line);
        mPicture = (ImageView) mConvertView.findViewById(R.id.id_alertDialog_picture);
        mButtonLine_one = (RelativeLayout) mConvertView.findViewById(R.id.id_alertDialog_button_line_one);
        mButtonLine_Two = (RelativeLayout) mConvertView.findViewById(R.id.id_alertDialog_button_line_two);
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPositiveListener != null) {
                    mPositiveListener.onClick();
                }
            }
        });
        mNeutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNeutralListener != null) {
                    mNeutralListener.onClick();
                }
            }
        });
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNegativeListener != null) {
                    mNegativeListener.onClick();
                }
            }
        });

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

    }

    public void setPositiveButton(String name, OnClickListener listener) {
        if (name != null) {
            mPositiveButton.setText(name.trim() + "");
        }
        mPositiveListener = listener;
        mPositiveButton.setVisibility(View.VISIBLE);
        isPositiveExist = true;
        if (isNeutralExist) {
            mButtonLine_one.setVisibility(View.VISIBLE);
        }
        if (isNegativeExist) {
            mButtonLine_Two.setVisibility(View.VISIBLE);
        }
    }

    public void setNegativeButton(String name, OnClickListener listener) {
        if (name != null) {
            mNegativeButton.setText(name.trim() + "");
        }
        mNegativeListener = listener;
        mNegativeButton.setVisibility(View.VISIBLE);
        isNegativeExist = true;
        if (isNeutralExist) {
            mButtonLine_Two.setVisibility(View.VISIBLE);
        }
        if (isPositiveExist) {
            mButtonLine_one.setVisibility(View.VISIBLE);
        }
    }

    public void setNeutralButton(String name, OnClickListener listener) {
        if (name != null) {
            mNeutralButton.setText(name.trim() + "");
        }
        mNeutralListener = listener;
        mNeutralButton.setVisibility(View.VISIBLE);
        isNeutralExist = true;
        if (isPositiveExist) {
            mButtonLine_one.setVisibility(View.VISIBLE);
        }
        if (isNegativeExist) {
            mButtonLine_Two.setVisibility(View.VISIBLE);
        }
    }

    public void addPicture(Bitmap picture) {
        mPicture.setImageBitmap(picture);
        mPicture.setVisibility(View.VISIBLE);
    }

    public void addPicture(int res) {
        mPicture.setImageResource(res);
        mPicture.setVisibility(View.VISIBLE);
    }

    public void setCancelAble(boolean isCancelable) {

        if (isCancelable) {
            this.setBackgroundDrawable(new BitmapDrawable());
        } else {
            this.setBackgroundDrawable(null);
        }
    }

    public void show() {
        this.showAtLocation(parentView, Gravity.CENTER, 0, 0);
    }

    public void setContent(String content) {
        if (content != null) {
            mContent.setText(content + "");
        }
        mContent.setVisibility(View.VISIBLE);
    }

    public void setTitle(String title) {
        if (title != null) {
            mTitle.setText(title + "");
        }
        mTitleContainer.setVisibility(View.VISIBLE);
        mTitle.setVisibility(View.VISIBLE);
        mTitleLine.setVisibility(View.VISIBLE);
    }

    public void setTitleIcon(Bitmap icon) {
        mTitleIcon.setImageBitmap(icon);
        mTitleContainer.setVisibility(View.VISIBLE);
        mTitleLine.setVisibility(View.VISIBLE);
        mTitleIcon.setVisibility(View.VISIBLE);
    }

    public void setTitleIcon(int res) {
        mTitleIcon.setImageResource(res);
        mTitleContainer.setVisibility(View.VISIBLE);
        mTitleLine.setVisibility(View.VISIBLE);
        mTitleIcon.setVisibility(View.VISIBLE);
    }
    public void enableMove(){
        //popupWindow的移动是以刚初始化的位置为坐标原点
        mConvertView.setOnTouchListener(new View.OnTouchListener() {
            int orgX = -1, orgY = -1;
            int offsetX, offsetY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        orgX = (int) event.getRawX() - lastX;
                        orgY = (int) event.getRawY() - lastY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        offsetX = (int) (event.getRawX() - orgX);
                        offsetY = (int) (event.getRawY() - orgY);
                        AlertDialogPopupWindow.this.update(offsetX, offsetY, -1, -1, false);
                        break;
                    case MotionEvent.ACTION_UP:
                        lastX = offsetX;
                        lastY = offsetY;
                        break;
                }
                return true;
                //return true;到此为止,截断事件传递
                //return false;go on传递给其他事件
            }
        });
    }
}
