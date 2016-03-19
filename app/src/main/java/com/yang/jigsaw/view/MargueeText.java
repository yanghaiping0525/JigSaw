package com.yang.jigsaw.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/2/26.
 */
public class MargueeText extends TextView {
    public MargueeText(Context context) {
        super(context);
    }

    public MargueeText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MargueeText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
