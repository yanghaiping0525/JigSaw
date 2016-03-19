package com.yang.jigsaw.camera.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.yang.jigsaw.utils.ScreenSize;

/**
 * Created by Administrator on 2016/2/11.
 */
public class ReferenceLine extends View {
    private Paint mLinePaint;
    private Context mContext;

    public ReferenceLine(Context context) {
        this(context, null);
    }

    public ReferenceLine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReferenceLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.parseColor("#45e0e0e0"));
        mLinePaint.setStrokeWidth(3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int screenWidth = ScreenSize.getDispalyMetrics(mContext).widthPixels;
        int screenHeight = ScreenSize.getDispalyMetrics(mContext).heightPixels;
        int width = screenWidth / 3;
        int top = (screenHeight - screenWidth) / 2;
        //int height = screenHeight / 3;
        //画垂直线
        for (int i = 0, j = 0; i <= screenWidth && j < 4; i += width, j++) {
            canvas.drawLine(i, top, i,top +  width * 3, mLinePaint);
        }
        //画水平线
        for (int i = 0, j = 0; i <= top + screenWidth && i < 4; i++, j += width) {
            canvas.drawLine(0, j + top, screenWidth, j + top, mLinePaint);
        }
    }
}
