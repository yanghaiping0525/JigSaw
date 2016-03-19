package com.yang.jigsaw.camera.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.yang.jigsaw.utils.ScreenSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by YangHaiPing on 2016/2/11.
 */
public class CameraPreview extends SurfaceView implements Camera.AutoFocusCallback {
    //预览高度
    private int viewHeight = 0;
    //预览宽度
    private int viewWidth = 0;
    //相机状态监听接口
    private OnCameraStatusListener mListener;
    private SurfaceHolder mSurfaceHolder;
    //监听预览窗口的创建、改变、销毁事件
    private MySurfaceCallBack mSurfaceCallBack;
    //硬件相机引用
    private Camera mCamera;
    //自定义对焦区域
    private FocusView mFocusView;
    private Context mContext;
    //屏幕宽度
    private int screenWidth;
    //屏幕高度
    private int screenHeight;
    //拍照结束的回调
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();
            if (mListener != null) {
                mListener.onCameraStopped(data);
            }
        }
    };

    public void setOnCameraStateListener(OnCameraStatusListener listener) {
        mListener = listener;
    }

    public void takePicture() {
        if (mCamera != null) {
            mCamera.takePicture(null, null, mPictureCallback);
        }
    }

    public void start() {
        if (mCamera != null)
            mCamera.startPreview();
    }

    public void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }


    public interface OnCameraStatusListener {
        void onCameraStopped(byte[] data);
    }

    public CameraPreview(Context context) {
        this(context, null);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //初始化预览界面
        mSurfaceCallBack = new MySurfaceCallBack();
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallBack);
        //表明该Surface不包含原生数据,Surface用到的数据由其他对象提供,不进行缓冲,提高效率
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //获得屏幕宽高
        screenWidth = ScreenSize.getWidth(context);
        screenHeight = ScreenSize.getHeight(context);
        //监听触摸事件完成对焦
        setOnTouchListener(onTouchListener);
    }

    //监听触摸事件,完成对焦
    OnTouchListener onTouchListener = new OnTouchListener() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //先获得对焦圈的宽高
                int width = mFocusView.getWidth();
                int height = mFocusView.getHeight();
                //设置对焦圈的位置已触摸点的位置为中心
                mFocusView.setX(event.getX() - (width / 2));
                mFocusView.setY(event.getY() - (height / 2));
                //开始执行对焦圈在对焦过程时的动画
                mFocusView.beginFocus();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (mCamera != null) {
                    //在手指释放位置的显示区域进行对焦
                    focusOnTouch(event);
                }
            }
            return true;
        }
    };

    private final class MySurfaceCallBack implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //获取相机信息,判断是否有有用的摄像头
            if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
                return;
            }
            //如果相机未创建则实例化
            if (mCamera == null) {
                mCamera = getCameraInstance();
            } else {
                Toast.makeText(mContext, "相机正在使用中", Toast.LENGTH_SHORT).show();
            }
            if (mCamera == null) {
                Toast.makeText(getContext(), "找不到摄像头！", Toast.LENGTH_SHORT).show();
                return;
            }
            //更新相机参数
            updateCameraParameters();
            try {
                //设置预览
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
                mCamera.release();
                mCamera = null;
            }

            if (mCamera != null) {
                //开始预览
                mCamera.startPreview();
            }
            setFocus();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mCamera != null) {
                mCamera.stopPreview();
                updateCameraParameters();
                try {
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.startPreview();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                setFocus();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        }
    }


    /**
     * 设置自动聚焦，并且聚焦的矩形显示在屏幕中间位置
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setFocus() {
        if (mFocusView != null && !mFocusView.isFocusing() && mCamera != null) {
            mCamera.autoFocus(this);
            mFocusView.setX((screenWidth - mFocusView.getWidth()) / 2);
            mFocusView.setY((screenHeight - mFocusView.getHeight()) / 2);
            mFocusView.beginFocus();
        }
    }

    //更新摄像头参数
    private void updateCameraParameters() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            //获得所有可以使用的对焦方式
            List<String> focusModes = parameters.getSupportedFocusModes();
            //如果支持连续自动对焦
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            //如果不支持连续自动对焦则设置为自动对焦
            else {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            //设置记录拍摄时间
            long time = new Date().getTime();
            parameters.setGpsTimestamp(time);
            //设置照片格式
            parameters.setPictureFormat(ImageFormat.JPEG);
            //设置为最高质量
            parameters.set("jpeg-quality", 100);
            //设置预览宽高为屏幕宽高
            Camera.Size previewSize = findPreviewSizeByScreen();
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            //如果屏幕显示方向为竖屏,将预览方向旋转90度,图片方向旋转90度,系统默认是横屏模式
            if (mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                mCamera.setDisplayOrientation(90);
                parameters.setRotation(90);
            }

            try {
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                //倘若上面预览设置失败,寻找最佳方案设置预览宽高
                Camera.Size previewSize_ = findBestPreviewSize(parameters);
                parameters.setPreviewSize(previewSize_.width, previewSize_.height);
                parameters.setPictureSize(previewSize_.width, previewSize_.height);
                mCamera.setParameters(parameters);
            }
        }
    }


    /**
     * 找到最合适的显示分辨率 （防止预览图像变形）
     *
     * @param parameters
     * @return
     */
    private Camera.Size findBestPreviewSize(Camera.Parameters parameters) {
        // 系统支持的所有预览分辨率
        String previewSizeValueString = null;
        //获得所有支持的预览尺寸
        previewSizeValueString = parameters.get("preview-size-values");
        //如果上一步失败则直接获得默认尺寸
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }
        //如果还是失败则继续设置为屏幕宽高
        if (previewSizeValueString == null) { // 有些手机例如m9获取不到支持的预览大小 就直接返回屏幕大小
            return mCamera.new Size(ScreenSize.getWidth(mContext),
                    ScreenSize.getHeight(mContext));
        }
        float bestX = 0;
        float bestY = 0;

        float tmpRatio = 0;
        float viewRatio = 0;

        if (viewWidth != 0 && viewHeight != 0) {
            viewRatio = Math.min((float) viewWidth, (float) viewHeight)
                    / Math.max((float) viewWidth, (float) viewHeight);
        }

        String[] COMMA_PATTERN = previewSizeValueString.split(",");
        for (String previewSizeString : COMMA_PATTERN) {
            previewSizeString = previewSizeString.trim();

            int dimPosition = previewSizeString.indexOf('x');
            if (dimPosition == -1) {
                continue;
            }

            float newX = 0;
            float newY = 0;

            try {
                newX = Float.parseFloat(previewSizeString.substring(0, dimPosition));
                newY = Float.parseFloat(previewSizeString.substring(dimPosition + 1));
            } catch (NumberFormatException e) {
                continue;
            }

            float radio = Math.min(newX, newY) / Math.max(newX, newY);
            if (tmpRatio == 0) {
                tmpRatio = radio;
                bestX = newX;
                bestY = newY;
            } else if (tmpRatio != 0 && (Math.abs(radio - viewRatio)) < (Math.abs(tmpRatio - viewRatio))) {
                tmpRatio = radio;
                bestX = newX;
                bestY = newY;
            }
        }

        if (bestX > 0 && bestY > 0) {
            return mCamera.new Size((int) bestX, (int) bestY);
        }
        return null;
    }


    /**
     * 将预览大小设置为屏幕大小
     *
     * @return
     */
    private Camera.Size findPreviewSizeByScreen() {
        if (viewWidth != 0 && viewHeight != 0) {
            return mCamera.new Size(Math.max(viewWidth, viewHeight),
                    Math.min(viewWidth, viewHeight));
        } else {
            return mCamera.new Size(ScreenSize.getHeight(mContext), ScreenSize.getWidth(mContext));
        }

    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            int cameraCount;
            //获取相机信息
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            //获得摄像头数量
            cameraCount = Camera.getNumberOfCameras();
            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                //facing代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                //如果找到后置摄像头
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        camera = Camera.open(i);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
                    }
                }
                //如果某些原因上一步失败打开默认的摄像头
                if (camera == null) {
                    camera = Camera.open(0);
                }
                //如果摄像头还是空,提醒用户,本案例中不使用前置摄像头
                if (camera == null) {
                    Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
        }
        return camera;
    }


    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }



    /**
     * 设置焦点和测光区域
     *
     * @param event
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void focusOnTouch(MotionEvent event) {

        int[] location = new int[2];
        RelativeLayout relativeLayout = (RelativeLayout) getParent();
        /*getLocationInWindow
                一个控件在其父窗口中的坐标位置
          getLocationOnScreen
                一个控件在其整个屏幕上的坐标位置*/
        relativeLayout.getLocationOnScreen(location);
        //getX()是表示Widget相对于自身左上角的x坐标,而getRawX()是表示相对于屏幕左上角的x坐标值
        Rect focusRect = calculateTapArea(mFocusView.getWidth(),
                mFocusView.getHeight(), 1f, event.getRawX(), event.getRawY(),
                location[0], location[0] + relativeLayout.getWidth(), location[1],
                location[1] + relativeLayout.getHeight());
        Rect meteringRect = calculateTapArea(mFocusView.getWidth(),
                mFocusView.getHeight(), 1.5f, event.getRawX(), event.getRawY(),
                location[0], location[0] + relativeLayout.getWidth(), location[1],
                location[1] + relativeLayout.getHeight());

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //设置焦点区域
        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(focusRect, 1000));
            parameters.setFocusAreas(focusAreas);
        }
        //设置测量区域
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));
            parameters.setMeteringAreas(meteringAreas);
        }

        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
        }
        mCamera.autoFocus(this);

    }

    public static Rect calculateTapArea(int focusWidth, int focusHeight,
                                        float areaMultiple, float x, float y, int previewLeft,
                                        int previewRight, int previewTop, int previewBottom) {
        int areaWidth = (int) (focusWidth * areaMultiple);
        int areaHeight = (int) (focusHeight * areaMultiple);
        int centerX = (previewLeft + previewRight) / 2;
        int centerY = (previewTop + previewBottom) / 2;
        double unitX = ((double) previewRight - (double) previewLeft) / 2000;
        double unitY = ((double) previewBottom - (double) previewTop) / 2000;
        int left = clamp((int) (((x - areaWidth / 2) - centerX) / unitX),
                -1000, 1000);
        int top = clamp((int) (((y - areaHeight / 2) - centerY) / unitY),
                -1000, 1000);
        int right = clamp((int) (left + areaWidth / unitX), -1000, 1000);
        int bottom = clamp((int) (top + areaHeight / unitY), -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    public static int clamp(int x, int min, int max) {
        if (x > max)
            return max;
        if (x < min)
            return min;
        return x;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        //设置测量模式为精确模式
        super.onMeasure(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
    }

    public void setFocusView(FocusView focusView) {
        this.mFocusView = focusView;
    }


}
