package com.yang.jigsaw.camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.yang.jigsaw.R;
import com.yang.jigsaw.camera.adapter.MyEffectSelectedAdapter;
import com.yang.jigsaw.camera.view.CameraPreview;
import com.yang.jigsaw.camera.view.FocusView;
import com.yang.jigsaw.camera.view.MyEffectSelectedView;
import com.yang.jigsaw.utils.ImageEffectHelper;
import com.yang.jigsaw.utils.ImageLoader;
import com.yang.jigsaw.utils.ScreenSize;
import com.yang.jigsaw.utils.ShowPopupWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyCamera extends AppCompatActivity implements CameraPreview.OnCameraStatusListener {
    //用于先生预览的自定义View
    private CameraPreview mCameraPreview;
    //拍照按钮,关闭按钮
    private ImageButton mCapture, mExit;
    //自定义对焦圈
    private FocusView mFocusView;
    //拍照后显示图片
    private ImageView mPhoto;
    //图片效果选择栏弹出按钮
    private ImageButton mPhotoEffectEditor;
    //保存各种效果的bitmap
    private Bitmap mBitmap, mBitmap_no_effect, mBitmap_blackAndWhite, mBitmap_comic, mBitmap_color_sketch, mBitmap_oil_paint, mBitmap_ice_effect, mBitmap_anti_color, mBitmap_old_photo, mBitmap_fresco, mBitmap_gray;
    //效果选择栏适配器
    private MyEffectSelectedAdapter mGalleryAdapter;
    //处理照片View
    private LinearLayout mEditView;
    //保存照片效果选择Icon的资源
    private List<Integer> mResourceIds = new ArrayList<>();
    //照片效果名称
    private List<String> mEffectNames = new ArrayList<>();
    //效果选择栏
    private MyEffectSelectedView mGallery;
    private int mEffect = 0;
    //被选中的效果选择栏中的icon
    private View mSelectedView;
    //屏幕宽高
    private int mScreenWidth, mScreenHeight;
    private static final int RESULT_OK = 0x200;
    //图片保存路径
    private String mFilePath;
    //显示各种照片效果
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 1:
                    mPhoto.setImageBitmap(mBitmap_blackAndWhite);
                    break;
                case 2:
                    mPhoto.setImageBitmap(mBitmap_comic);
                    break;
                case 3:
                    mPhoto.setImageBitmap(mBitmap_color_sketch);
                    break;
                case 4:
                    mPhoto.setImageBitmap(mBitmap_oil_paint);
                    break;
                case 5:
                    mPhoto.setImageBitmap(mBitmap_ice_effect);
                    break;
                case 6:
                    mPhoto.setImageBitmap(mBitmap_anti_color);
                    break;
                case 7:
                    mPhoto.setImageBitmap(mBitmap_old_photo);
                    break;
                case 8:
                    mPhoto.setImageBitmap(mBitmap_fresco);
                    break;
                case 9:
                    mPhoto.setImageBitmap(mBitmap_gray);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置窗体始终点亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera);
        initFindViewById();
        initEvent();
    }


    private void initEvent() {
        //设置对焦显示圈
        mCameraPreview.setFocusView(mFocusView);
        //监听拍照结束事件,处理获得的图片数据
        mCameraPreview.setOnCameraStateListener(this);
        //获得屏幕宽高
        mScreenHeight = ScreenSize.getHeight(this);
        mScreenWidth = ScreenSize.getWidth(this);
        //设置效果栏资源
        final Integer[] resIds = new Integer[]{R.mipmap.no_effect, R.mipmap.blackandwhite_effect, R.mipmap.comic_effect, R.mipmap.color_sketch_effect, R.mipmap.oil_paint_effect, R.mipmap.ice_effect, R.mipmap.anti_color_effect, R.mipmap.old_photo_effect, R.mipmap.fresco_effect, R.mipmap.gray_effect};
        for (int i = 0; i < resIds.length; i++) {
            mResourceIds.add(resIds[i]);
        }
        //设置效果栏资源名称
        final String[] effectNames = new String[]{"无效果", "黑白素描", "漫画", "彩色素描", "油画", "冰冻", "反色", "老照片", "壁画", "灰调"};
        for (int i = 0; i < effectNames.length; i++) {
            mEffectNames.add(effectNames[i]);
        }
        //初始化效果栏数据
        mGalleryAdapter = new MyEffectSelectedAdapter(MyCamera.this, mResourceIds, mEffectNames);
        //监听拍照按钮事件
        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    takePhoto();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //监听退出按钮事件
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCamera.this.finish();
            }
        });
        //监听效果按钮事件
        mPhotoEffectEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初始化并弹出效果栏
                View view = LayoutInflater.from(MyCamera.this).inflate(R.layout.edit_picture_popup_window, null);
                mGallery = (MyEffectSelectedView) view.findViewById(R.id.id_gallery);
                final PopupWindow popupWindow = new PopupWindow(view);
                popupWindow.setHeight(mScreenHeight / 6);
                popupWindow.setWidth(mScreenWidth);
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
                //显示效果选择栏
                mGallery.setAdapter(mGalleryAdapter);
                //监听效果点击事件处理各种效果
                mGallery.setOnItemClickListener(new MyEffectSelectedView.OnItemClickListener() {
                    @Override
                    public void onClick(View view, int pos) {
                        view.setBackgroundColor(Color.parseColor("#AA024DA4"));
                        mGalleryAdapter.recordClickPosition(pos);
                        mSelectedView = view;
                        switch (pos) {
                            case 0:
                                mEffect = 0;
                                mPhoto.setImageBitmap(mBitmap_no_effect);
                                break;
                            case 1:
                                if (mBitmap_blackAndWhite == null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mBitmap_blackAndWhite = ImageEffectHelper.blackAndWhiteSketchEffect(mBitmap_no_effect);
                                            Message message = Message.obtain();
                                            message.what = 1;
                                            message.obj = mBitmap_blackAndWhite;
                                            mHandler.sendMessage(message);
                                        }
                                    }.start();

                                } else {
                                    mPhoto.setImageBitmap(mBitmap_blackAndWhite);
                                }
                                mEffect = 1;
                                break;
                            case 2:
                                if (mBitmap_comic == null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mBitmap_comic = ImageEffectHelper.comicEffect(mBitmap_no_effect);
                                            Message message = Message.obtain();
                                            message.what = 2;
                                            message.obj = mBitmap_comic;
                                            mHandler.sendMessage(message);
                                        }
                                    }.start();

                                } else {
                                    mPhoto.setImageBitmap(mBitmap_comic);
                                }
                                mEffect = 2;
                                break;
                            case 3:
                                if (mBitmap_color_sketch == null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mBitmap_color_sketch = ImageEffectHelper.colorSketchEffect(mBitmap_no_effect);
                                            Message message = Message.obtain();
                                            message.what = 3;
                                            message.obj = mBitmap_color_sketch;
                                            mHandler.sendMessage(message);
                                        }
                                    }.start();

                                } else {
                                    mPhoto.setImageBitmap(mBitmap_color_sketch);
                                }
                                mEffect = 3;
                                break;
                            case 4:
                                if (mBitmap_oil_paint == null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mBitmap_oil_paint = ImageEffectHelper.oilPaintingEffect(mBitmap_no_effect);
                                            Message message = Message.obtain();
                                            message.what = 4;
                                            message.obj = mBitmap_oil_paint;
                                            mHandler.sendMessage(message);
                                        }
                                    }.start();

                                } else {
                                    mPhoto.setImageBitmap(mBitmap_oil_paint);
                                }
                                mEffect = 4;
                                break;
                            case 5:
                                if (mBitmap_ice_effect == null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mBitmap_ice_effect = ImageEffectHelper.ice(mBitmap_no_effect);
                                            Message message = Message.obtain();
                                            message.what = 5;
                                            message.obj = mBitmap_ice_effect;
                                            mHandler.sendMessage(message);
                                        }
                                    }.start();

                                } else {
                                    mPhoto.setImageBitmap(mBitmap_ice_effect);
                                }
                                mEffect = 5;
                                break;
                            case 6:
                                if (mBitmap_anti_color == null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mBitmap_anti_color = ImageEffectHelper.antiColorEffect(mBitmap_no_effect);
                                            Message message = Message.obtain();
                                            message.what = 6;
                                            message.obj = mBitmap_anti_color;
                                            mHandler.sendMessage(message);
                                        }
                                    }.start();

                                } else {
                                    mPhoto.setImageBitmap(mBitmap_anti_color);
                                }
                                mEffect = 6;
                                break;
                            case 7:
                                if (mBitmap_old_photo == null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mBitmap_old_photo = ImageEffectHelper.oldPhotoEffect(mBitmap_no_effect);
                                            Message message = Message.obtain();
                                            message.what = 7;
                                            message.obj = mBitmap_old_photo;
                                            mHandler.sendMessage(message);
                                        }
                                    }.start();

                                } else {
                                    mPhoto.setImageBitmap(mBitmap_old_photo);
                                }
                                mEffect = 7;
                                break;
                            case 8:
                                if (mBitmap_fresco == null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mBitmap_fresco = ImageEffectHelper.frescoEffect(mBitmap_no_effect);
                                            Message message = Message.obtain();
                                            message.what = 8;
                                            message.obj = mBitmap_fresco;
                                            mHandler.sendMessage(message);
                                        }
                                    }.start();

                                } else {
                                    mPhoto.setImageBitmap(mBitmap_fresco);
                                }
                                mEffect = 8;
                                break;
                            case 9:
                                if (mBitmap_gray == null) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mBitmap_gray = ImageEffectHelper.grayEffect(mBitmap_no_effect);
                                            Message message = Message.obtain();
                                            message.what = 9;
                                            message.obj = mBitmap_gray;
                                            mHandler.sendMessage(message);
                                        }
                                    }.start();

                                } else {
                                    mPhoto.setImageBitmap(mBitmap_gray);
                                }
                                mEffect = 9;
                                break;
                        }
                    }
                });
                //拓展
                mGallery.setOnViewChangeListener(new MyEffectSelectedView.OnViewChangeListener() {
                    @Override
                    public void onViewChange(View view, int pos) {

                    }
                });
                //设置效果选择栏弹出效果
                popupWindow.setAnimationStyle(R.style.PopupWindow_AddPhoto_Animation);
                //设置效果选择栏位置并显示
                ShowPopupWindow.showPopupWindowDropDown(popupWindow, MyCamera.this.findViewById(R.id.id_edit_view));
            }
        });
    }

    private void initFindViewById() {
        mCapture = (ImageButton) findViewById(R.id.id_imageButton_capture);
        mPhoto = (ImageView) findViewById(R.id.id_photo_taken);
        mEditView = (LinearLayout) findViewById(R.id.id_photo_edit);
        mCameraPreview = (CameraPreview) findViewById(R.id.id_surface_camera);
        mFocusView = (FocusView) findViewById(R.id.id_focusView);
        mExit = (ImageButton) findViewById(R.id.id_exit_camera);
        mPhotoEffectEditor = (ImageButton) findViewById(R.id.id_photo_effect);
    }


    public void takePhoto() {
        if (mCameraPreview != null) {
            mCameraPreview.takePicture();
        }
    }

    //照相结束后的回调,处理照片数据
    @Override
    public void onCameraStopped(byte[] data) {
        //获得照片bitmap对象
        mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        //压缩图片，大小为屏幕大小
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        mBitmap_no_effect = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = ImageLoader.getInstance().calculateInSampleSize(options, mScreenWidth, mScreenHeight);
        options.inJustDecodeBounds = false;
        mBitmap_no_effect = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        mCameraPreview.stop();
        //显示图片编辑区域
        mEditView.setVisibility(View.VISIBLE);
        //显示压缩后的图片
        mPhoto.setImageBitmap(mBitmap_no_effect);
        mEffect = 0;
    }

    //点击栏取消按钮
    public void cancelPicket(View view) {
        mBitmap_blackAndWhite = mBitmap_comic = mBitmap_color_sketch = mBitmap_oil_paint = mBitmap_ice_effect = mBitmap_anti_color = mBitmap_old_photo = mBitmap_fresco = mBitmap_gray = null;
        mGalleryAdapter.recordClickPosition(0);
        mEffect = 0;
        if (mSelectedView != null) {
            //还原效果栏中被点击效果的背景颜色
            mSelectedView.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        //隐藏编辑界面
        mEditView.setVisibility(View.GONE);
        //还原状态栏初始显示图标
        mGallery.initFirstScreenChildren(MyEffectSelectedView.mMaxCountOnScreen);
        //重新拍照预览
        mCameraPreview.start();
    }

    public void editFinish(View view) {
        //根据系统时间定义照片名字
        long dateTaken = System.currentTimeMillis();
        String filename = "IMG" + DateFormat.format("yyyy-MM-dd kk-mm-ss", dateTaken)
                .toString() + ".jpg";
        //mFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/" + filename;
        //设置图片保存路径
        mFilePath = MyCamera.this.getFilesDir() + "/" + filename;
        OutputStream outputStream = null;
        try {
            File file = new File(mFilePath);
            outputStream = new FileOutputStream(file);
            //保存最终选择的照片效果到定义的路径
            switch (mEffect) {
                case 0:
                    mBitmap_no_effect.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
                case 1:
                    mBitmap_blackAndWhite.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
                case 2:
                    mBitmap_comic.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
                case 3:
                    mBitmap_color_sketch.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
                case 4:
                    mBitmap_oil_paint.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
                case 5:
                    mBitmap_ice_effect.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
                case 6:
                    mBitmap_anti_color.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
                case 7:
                    mBitmap_old_photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
                case 8:
                    mBitmap_fresco.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
                case 9:
                    mBitmap_gray.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    break;
            }
            //通知系统有扫描指定路径的图片进行挂载
            MediaScannerConnection.scanFile(MyCamera.this, new String[]{mFilePath}, null, null);
            mBitmap_blackAndWhite = mBitmap_comic = mBitmap_color_sketch = mBitmap_oil_paint = mBitmap_ice_effect = mBitmap_anti_color = mBitmap_old_photo = mBitmap_fresco = mBitmap_gray = null;
            //将新的图片路径添加到图片路径配置文件中
            SharedPreferences sharedPreferences = getSharedPreferences("imagePaths", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> filePaths = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                filePaths = sharedPreferences.getStringSet("imagePaths", new HashSet<String>());
            }

            if (!filePaths.contains(mFilePath)) {
                filePaths.add(mFilePath);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.clear();
                editor.commit();
                editor.putStringSet("imagePaths", filePaths);
                editor.commit();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //通知上一个Activity或者Fragment执行onActivityResult刷新数据
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        this.finish();
    }


    private void invalidateMethod() {
        //全屏(在v7兼容包会报错)
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //使窗口支持透明度
        //getWindow().setFormat(PixelFormat.TRANSLUCENT);


        //设置窗体背景模糊
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
//                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    }

}
