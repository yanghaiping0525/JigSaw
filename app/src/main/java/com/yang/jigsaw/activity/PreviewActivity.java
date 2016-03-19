package com.yang.jigsaw.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.yang.jigsaw.R;
import com.yang.jigsaw.bean.ImagesPathsBean;
import com.yang.jigsaw.utils.ActionBarHelper;
import com.yang.jigsaw.utils.DensityUtil;
import com.yang.jigsaw.utils.ImageEffectHelper;
import com.yang.jigsaw.utils.ImageLoader;
import com.yang.jigsaw.utils.ScreenSize;
import com.yang.jigsaw.utils.StatusBarHelper;
import com.yang.jigsaw.utils.SystemBarTintManager;
import com.yang.jigsaw.view.AlertDialogPopupWindow;

/**
 * Created by Administrator on 2016/1/31.
 */
public class PreviewActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private ViewPager mViewPager;
    private static ImageView[] mImageViews;
    private PagerAdapter mAdapter;
    private Set<String> mselectedPicsPaths = new HashSet<>();
    private String[] mImagesPaths;
    private ImageView mCurrentImage;
    private String mCurrentImagePath;
    private int mDeleteCount = 0;
    private static final int MAX_VALUE = 255;
    private static final int MID_VALUE = 127;
    private final static int RESULT_DELETE = 0x33;
    private final static int RESULT_NEW_PHOTO = 0x44;
    private float mHue, mSaturation, mLightness;
    private Bitmap mCurrentBitmap;
    private Bitmap[] mBitmaps;
    private boolean once = false, once_ = false;
    private TextView mHueValue, mSaturationValue, mLightnessValue;
    private String mFilePath;
    private boolean mHasNewPhoto = false;
    private int lastX = 0, lastY = 0;
    private List<TextView> mEffectList = new ArrayList<>();
    private TextView normal, black_and_white, comic, color_sketch, oil_paint, ice_effect, anti_color, old_photo, fresco, gray;
    private boolean isSpread = false;
    private int mSpreadScope;
    private Bitmap mCopyBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_viewpager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.actionbar);//通知栏所需颜色
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            getWindow().setBackgroundDrawableResource(R.color.actionbar);
        }
        final Intent intent = getIntent();
        ImagesPathsBean pathsBean = (ImagesPathsBean) intent.getSerializableExtra("imagesPaths");
        mselectedPicsPaths = pathsBean.getPaths();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        final int width = displayMetrics.widthPixels;
        final int height = displayMetrics.heightPixels;
        final int imagesCount = mselectedPicsPaths.size();
        mSpreadScope = DensityUtil.dip2px(this, 30);
        mImagesPaths = new String[imagesCount];
        Iterator iterator = mselectedPicsPaths.iterator();
        for (int i = 0; i < imagesCount; i++) {
            mImagesPaths[i] = (String) iterator.next();
            Log.i("Path", "path " + i + " : " + mImagesPaths[i]);
        }
        mImageViews = new ImageView[imagesCount];
        mBitmaps = new Bitmap[imagesCount];
        mViewPager = (ViewPager) findViewById(R.id.id_viewPager);
        mAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return imagesCount;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                final ImageView mImageView = new ImageView(PreviewActivity.this);
                if (mImageViews[position] == null) {
                    /*BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(mImagesPaths[position], options);
                    options.inSampleSize = mImageLoader.calculateInSampleSize(options, width, height);
                    options.inJustDecodeBounds = false;
                    final Bitmap bitmap = BitmapFactory.decodeFile(mImagesPaths[position], options);*/
                    Bitmap bitmap = ImageLoader.getInstance().decodeBitmapFromPath(mImagesPaths[position], width, height);
                    if (bitmap == null) {
                        mImageView.setImageResource(R.mipmap.pictures_no);
                        container.addView(mImageView);
                        mImageViews[position] = mImageView;
                        return mImageView;
                    }
                    mImageView.setImageBitmap(bitmap);
                    mImageViews[position] = mImageView;
                    mBitmaps[position] = bitmap;
                    if (!once_) {
                        if (position == 0) {
                            if (!once) {
                                mCopyBitmap = mCurrentBitmap = bitmap;
                                mCurrentImage = mImageViews[position];
                                once = true;
                                once_ = true;
                            }
                        }
                    }
                }
                container.addView(mImageViews[position]);
                return mImageViews[position];
            }


            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mImageViews[position]);
            }

            @Override
            public int getItemPosition(Object object) {
                //要想ViewPager的内容发生变化及notifyDataSetChanged()生效，得return POSITION_NONE
                return POSITION_NONE;
            }
        };
        mViewPager.setAdapter(mAdapter);
        mCurrentImagePath = mImagesPaths[0];

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.e("PageChange", "onPageScrolled position = " + position + " , positionOffset = " + positionOffset + " , positionOffsetPixels = " + positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                //Log.e("PageChange", "onPageSelected position = " + position);
                mCurrentImage = mImageViews[position];
                mCurrentImagePath = mImagesPaths[position];
                mCopyBitmap = mCurrentBitmap = mBitmaps[position];
            }

            @Override
            public void onPageScrollStateChanged(int state) {
               // Log.e("PageChange", "onPageScrollStateChanged state = " + state);
            }
        });
        View customView = LayoutInflater.from(this).inflate(R.layout.edit_selected_picture_actionbar, null);
        ActionBarHelper.showCustomActionBar(getSupportActionBar(), customView);
        ImageButton back = (ImageButton) customView.findViewById(R.id.id_actionbar_back);
        ImageView more = (ImageView) customView.findViewById(R.id.id_actionbar_more);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasNewPhoto) {
                    Intent intent = new Intent();
                    setResult(RESULT_NEW_PHOTO, intent);
                } else if (mDeleteCount > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("deleteCount", mDeleteCount);
                    setResult(RESULT_DELETE, intent);
                }
                PreviewActivity.this.finish();
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentView = LayoutInflater.from(PreviewActivity.this).inflate(R.layout.preview_viewpager, null);
                View popupView = LayoutInflater.from(PreviewActivity.this).inflate(R.layout.preview_edit_photo_popup_window, null);
                //容易报omm错误
                //View parentView = getWindow().getDecorView().findViewById(android.R.id.content);
                LinearLayout delete = (LinearLayout) popupView.findViewById(R.id.id_delete_picture_from_phone);
                LinearLayout edit = (LinearLayout) popupView.findViewById(R.id.id_edit_picture);
                // final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                final PopupWindow popupWindow = new PopupWindow(popupView);
                popupWindow.setHeight(ScreenSize.getHeight(PreviewActivity.this) / 5);
                popupWindow.setWidth(ScreenSize.getWidth(PreviewActivity.this) * 2 / 5);
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
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final File file = new File(mCurrentImagePath);
                        if (file.isFile() && file.exists()) {
                            final AlertDialogPopupWindow alertDialogPopupWindow = new AlertDialogPopupWindow(PreviewActivity.this);
                            alertDialogPopupWindow.setPositiveButton("确定", new AlertDialogPopupWindow.OnClickListener() {
                                @Override
                                public void onClick() {
                                    boolean isDelete = file.delete();
                                    if (isDelete) {
                                        mDeleteCount++;
                                        Toast.makeText(PreviewActivity.this, "图片已删除", Toast.LENGTH_SHORT).show();
                                        alertDialogPopupWindow.dismiss();
                                    }
                                }
                            });
                            alertDialogPopupWindow.setNegativeButton("取消", new AlertDialogPopupWindow.OnClickListener() {
                                @Override
                                public void onClick() {
                                    alertDialogPopupWindow.dismiss();
                                }
                            });
                            alertDialogPopupWindow.setContent("此操作将会从手机中删除所选图片，是否继续本次操作？");
                            alertDialogPopupWindow.setTitle("警告！");
                            alertDialogPopupWindow.addPicture(R.mipmap.shock);
                            alertDialogPopupWindow.show();
//                            AlertDialog.Builder builder = new AlertDialog.Builder(PreviewActivity.this);
//                            builder.setTitle("提示");
//                            builder.setMessage("是否从手机中删除该图片");
//                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                    boolean isDelete = file.delete();
//                                    if (isDelete) {
//                                        mDeleteCount++;
//                                        Toast.makeText(PreviewActivity.this, "图片已删除", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//
//                            AlertDialog dialog = builder.create();
//                            dialog.show();
                        } else {
                            Toast.makeText(PreviewActivity.this, "该图片已经不存在", Toast.LENGTH_SHORT).show();
                        }
                        popupWindow.dismiss();
                    }
                });
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getSupportActionBar().hide();
                        // fullScreen(true);
                        //View parentView = LayoutInflater.from(PreviewActivity.this).inflate(R.layout.preview_viewpager, null);
                        View parentView = getWindow().getDecorView().findViewById(android.R.id.content);
                        View popupView = LayoutInflater.from(PreviewActivity.this).inflate(R.layout.edit_photo_preview_popup_window, null);
                        final PopupWindow popupWindow_edit = new PopupWindow(popupView);
                        final SeekBar hueSB = (SeekBar) popupView.findViewById(R.id.id_sb_hue);
                        final SeekBar saturationSB = (SeekBar) popupView.findViewById(R.id.id_sb_saturation);
                        final SeekBar lightnessSB = (SeekBar) popupView.findViewById(R.id.id_sb_lightness);
                        mHueValue = (TextView) popupView.findViewById(R.id.id_sb_hue_value);
                        mSaturationValue = (TextView) popupView.findViewById(R.id.id_sb_saturation_value);
                        mLightnessValue = (TextView) popupView.findViewById(R.id.id_sb_lightness_value);
                        TextView ok = (TextView) popupView.findViewById(R.id.id_sb_ok);
                        TextView reset = (TextView) popupView.findViewById(R.id.id_sb_reset);
                        TextView cancel = (TextView) popupView.findViewById(R.id.id_sb_cancel);
                        TextView effect = (TextView) popupView.findViewById(R.id.id_effect);
                        normal = (TextView) popupView.findViewById(R.id.id_effect_normal);
                        black_and_white = (TextView) popupView.findViewById(R.id.id_effect_black_and_white);
                        comic = (TextView) popupView.findViewById(R.id.id_effect_comic);
                        color_sketch = (TextView) popupView.findViewById(R.id.id_effect_color_sketch);
                        oil_paint = (TextView) popupView.findViewById(R.id.id_effect_oil_paint);
                        ice_effect = (TextView) popupView.findViewById(R.id.id_effect_ice_effect);
                        anti_color = (TextView) popupView.findViewById(R.id.id_effect_anti_color);
                        old_photo = (TextView) popupView.findViewById(R.id.id_effect_old_photo);
                        fresco = (TextView) popupView.findViewById(R.id.id_effect_fresco);
                        gray = (TextView) popupView.findViewById(R.id.id_effect_gray);
                        mEffectList.clear();
                        mEffectList.add(normal);
                        mEffectList.add(black_and_white);
                        mEffectList.add(comic);
                        mEffectList.add(color_sketch);
                        mEffectList.add(oil_paint);
                        mEffectList.add(ice_effect);
                        mEffectList.add(anti_color);
                        mEffectList.add(old_photo);
                        mEffectList.add(fresco);
                        mEffectList.add(gray);

                        normal.setOnClickListener(this);
                        black_and_white.setOnClickListener(this);
                        comic.setOnClickListener(this);
                        color_sketch.setOnClickListener(this);
                        oil_paint.setOnClickListener(this);
                        ice_effect.setOnClickListener(this);
                        anti_color.setOnClickListener(this);
                        old_photo.setOnClickListener(this);
                        fresco.setOnClickListener(this);
                        gray.setOnClickListener(this);
                        isSpread = false;

                        hueSB.setMax(MAX_VALUE);
                        saturationSB.setMax(MAX_VALUE);
                        lightnessSB.setMax(MAX_VALUE);
                        hueSB.setOnSeekBarChangeListener(PreviewActivity.this);
                        saturationSB.setOnSeekBarChangeListener(PreviewActivity.this);
                        lightnessSB.setOnSeekBarChangeListener(PreviewActivity.this);
                        hueSB.setProgress(MID_VALUE);
                        saturationSB.setProgress(MID_VALUE);
                        lightnessSB.setProgress(MID_VALUE);


                        // popupWindow_edit.setHeight(ScreenSize.getHeight(PreviewActivity.this) * 5 / 16);
                        popupWindow_edit.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                        popupWindow_edit.setWidth(ScreenSize.getWidth(PreviewActivity.this));

                        //设置popupWindow点击外部不消失,且外部可以触摸
                        // popupWindow_edit.setBackgroundDrawable(new BitmapDrawable());
                        popupWindow_edit.setFocusable(true);
                        popupWindow_edit.setTouchable(true);
                        popupWindow_edit.setOutsideTouchable(true);
//
                        popupWindow_edit.showAtLocation(parentView, Gravity.TOP, 0, StatusBarHelper.getStatusHeight(PreviewActivity.this));
                        popupView.setOnTouchListener(new View.OnTouchListener() {
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
                                        popupWindow_edit.update(offsetX, offsetY, -1, -1, true);
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        lastX = offsetX;
                                        lastY = offsetY;
                                        break;
                                }
                                return false;
                            }
                        });
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getSupportActionBar().show();
                                long dateTaken = System.currentTimeMillis();
                                String filename = "IMG" + DateFormat.format("yyyy-MM-dd kk-mm-ss", dateTaken)
                                        .toString() + ".jpg";
                                mFilePath = PreviewActivity.this.getFilesDir() + "/" + filename;
                                File file = new File(mFilePath);
                                OutputStream outputStream = null;
                                try {
                                    mCurrentImage.setDrawingCacheEnabled(true);
                                    outputStream = new FileOutputStream(file);
                                    mCurrentImage.buildDrawingCache();
                                    Bitmap bitmap = mCurrentImage.getDrawingCache();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                    MediaScannerConnection.scanFile(PreviewActivity.this, new String[]{mFilePath}, null, null);
                                    mHasNewPhoto = true;
                                    Toast.makeText(PreviewActivity.this, "图片已保存至游戏目录", Toast.LENGTH_SHORT).show();
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
                                popupWindow_edit.dismiss();
                            }
                        });
                        reset.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hueSB.setProgress(MID_VALUE);
                                saturationSB.setProgress(MID_VALUE);
                                lightnessSB.setProgress(MID_VALUE);
                            }
                        });
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                hueSB.setProgress(MID_VALUE);
                                saturationSB.setProgress(MID_VALUE);
                                lightnessSB.setProgress(MID_VALUE);
                                mCurrentImage.setImageBitmap(ImageEffectHelper.handleImageEffect(mCopyBitmap, mHue, mSaturation, mLightness));
                                popupWindow_edit.dismiss();
                                getSupportActionBar().show();
                                // fullScreen(false);
                            }
                        });
                        effect.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startAnim();
                            }
                        });
                        normal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentImage.setImageBitmap(mCopyBitmap);
                                mCurrentBitmap = mCopyBitmap;
                            }
                        });
                        black_and_white.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentBitmap = ImageEffectHelper.blackAndWhiteSketchEffect(mCopyBitmap);
                                mCurrentImage.setImageBitmap(mCurrentBitmap);
                            }
                        });
                        comic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentBitmap = ImageEffectHelper.comicEffect(mCopyBitmap);
                                mCurrentImage.setImageBitmap(mCurrentBitmap);
                            }
                        });
                        color_sketch.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentBitmap = ImageEffectHelper.colorSketchEffect(mCopyBitmap);
                                mCurrentImage.setImageBitmap(mCurrentBitmap);
                            }
                        });
                        oil_paint.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentBitmap = ImageEffectHelper.oilPaintingEffect(mCopyBitmap);
                                mCurrentImage.setImageBitmap(mCurrentBitmap);
                            }
                        });
                        ice_effect.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentBitmap = ImageEffectHelper.ice(mCopyBitmap);
                                mCurrentImage.setImageBitmap(mCurrentBitmap);
                            }
                        });
                        anti_color.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentBitmap = ImageEffectHelper.antiColorEffect(mCopyBitmap);
                                mCurrentImage.setImageBitmap(mCurrentBitmap);
                            }
                        });
                        old_photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentBitmap = ImageEffectHelper.oldPhotoEffect(mCopyBitmap);
                                mCurrentImage.setImageBitmap(mCurrentBitmap);
                            }
                        });
                        fresco.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentBitmap = ImageEffectHelper.frescoEffect(mCopyBitmap);
                                mCurrentImage.setImageBitmap(mCurrentBitmap);
                            }
                        });
                        gray.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentBitmap = ImageEffectHelper.grayEffect(mCopyBitmap);
                                mCurrentImage.setImageBitmap(mCurrentBitmap);
                            }
                        });
                        popupWindow.dismiss();
                    }
                });
                popupWindow.showAtLocation(parentView, Gravity.RIGHT | Gravity.TOP, 0, StatusBarHelper.getStatusHeight(PreviewActivity.this) + (int) ActionBarHelper.getActionBarHeight(PreviewActivity.this) + 25);
            }
        });
    }

    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 调用NavUtils.getParentActivityIntent()方法可以获取到跳转至父Activity的Intent，
                // 然后如果父Activity和当前Activity是在同一个Task中的，则直接调用navigateUpTo()方法进行跳转，
                // 如果不是在同一个Task中的，则需要借助TaskStackBuilder来创建一个新的Task。
                // 这样子返回和单独在Menifest文件配置activity属性meta-data或parentActivityName比较，无需再重新创建activity
                // 与返回键类似，但是返回键是返回上一步操作状态，以下操作是返回上一个任务界面
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            // break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.id_sb_hue:
                mHue = (progress - MID_VALUE) * 1f / MID_VALUE * 180;
                mHueValue.setText((Math.round(((float) progress / MAX_VALUE) * 100) - 50) + "");
                break;
            case R.id.id_sb_saturation:
                mSaturation = progress * 1f / MID_VALUE;
                mSaturationValue.setText((Math.round(((float) progress / MAX_VALUE) * 100) - 50) + "");
                break;
            case R.id.id_sb_lightness:
                mLightness = progress * 1f / MID_VALUE;
                mLightnessValue.setText((Math.round(((float) progress / MAX_VALUE) * 100) - 50) + "");
                break;
        }
        mCurrentImage.setImageBitmap(ImageEffectHelper.handleImageEffect(mCurrentBitmap, mHue, mSaturation, mLightness));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAnim() {
        final int len = mEffectList.size();
        for (int i = 0; i < len; i++) {
            float yStart = !isSpread ? 0F : ((i + 1) * mSpreadScope);
            float yEnd = !isSpread ? ((i + 1) * mSpreadScope) : 0F;

            ObjectAnimator animator = ObjectAnimator.ofFloat(mEffectList.get(i), "translationY", yStart, yEnd);
            animator.setDuration(500);
            animator.setInterpolator(new BounceInterpolator());
            final int index = i;
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (!isSpread) {
                        normal.setVisibility(View.VISIBLE);
                        black_and_white.setVisibility(View.VISIBLE);
                        comic.setVisibility(View.VISIBLE);
                        color_sketch.setVisibility(View.VISIBLE);
                        oil_paint.setVisibility(View.VISIBLE);
                        ice_effect.setVisibility(View.VISIBLE);
                        anti_color.setVisibility(View.VISIBLE);
                        old_photo.setVisibility(View.VISIBLE);
                        fresco.setVisibility(View.VISIBLE);
                        gray.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if ((index == (len - 1)) && !isSpread) {
                        normal.setVisibility(View.GONE);
                        black_and_white.setVisibility(View.GONE);
                        comic.setVisibility(View.GONE);
                        color_sketch.setVisibility(View.GONE);
                        oil_paint.setVisibility(View.GONE);
                        ice_effect.setVisibility(View.GONE);
                        anti_color.setVisibility(View.GONE);
                        old_photo.setVisibility(View.GONE);
                        fresco.setVisibility(View.GONE);
                        gray.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
        isSpread = !isSpread;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mHasNewPhoto) {
                Intent intent = new Intent();
                setResult(RESULT_NEW_PHOTO, intent);
            } else if (mDeleteCount > 0) {
                Intent intent = new Intent();
                intent.putExtra("deleteCount", mDeleteCount);
                setResult(RESULT_DELETE, intent);
            }
            PreviewActivity.this.finish();
        }
        return false;
    }
}
