package com.yang.jigsaw.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.yang.jigsaw.R;
import com.yang.jigsaw.adapter.ImageAdapter;
import com.yang.jigsaw.bean.FolderBean;
import com.yang.jigsaw.bean.ImagesPathsBean;
import com.yang.jigsaw.utils.SystemBarTintManager;
import com.yang.jigsaw.view.AlertDialogPopupWindow;
import com.yang.jigsaw.view.DirListPopupWindow;

public class ImageLoaderActivity extends AppCompatActivity implements ImageAdapter.OnImageChoiceListener {
    //图片容器
    private GridView mGridView;
    //自定义图片适配器
    private ImageAdapter mImageAdapter;
    //图片选择器下方区域,用于显示文件夹名称与文件夹图片数量
    private RelativeLayout mBottomLayout;
    //用于显示包含图片的文件夹列表
    private DirListPopupWindow mPopupWindow;
    //显示文件夹名称,显示该文件夹下图片的数量
    private TextView mDirName, mPhotoNum;
    //图片的名称集合
    private List<String> mImagesNames;
    //当前文件夹
    private File mCurrentDir;
    //当前文件夹图片数量
    private int mCurrentDirPicsCount;
    //所有包含图片的文件夹信息集合
    private List<FolderBean> mFolderBeans = new ArrayList<>();
    //进度条
    private ProgressDialog mProgressDialog;
    private static final int PICS_LOADED = 0xA;
    //已选择的图片的路径集合
    private Set<String> mSelectedPicsPaths;
    private static final int RESULT_OK = 0x200;
    private static final int REQUEST_CODE = 0x99;
    //ActionBar上的按钮:返回、预览及编辑、选择、删除
    private ImageButton mBack, mPreview, mChoice, mDelete;
    //ActionBar上的编辑区域
    private RelativeLayout mEditView;
    //标题栏显示文字
    private TextView mTitle;
    //总共删除的图片的数量
    private int mTotalDeleteCount;
    //当前文件夹在文件夹集合的索引位置
    private int mCurrentDirPosition = -1;
    //返回的文件夹
    private File mGoBackDir = null;
    //返回的文件夹的图片的数量
    private int mGoBackDirPicsCount;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == PICS_LOADED) {
                mProgressDialog.dismiss();
                mBottomLayout.setVisibility(View.VISIBLE);
                showPics();
                initDirsPopupWindow();
            }
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_selector);
        //设置状态栏显示颜色
        setStateBarColor();
        //设置ActionBar并设置点击事件
        initActionBar();
        //初始化View
        initView();
        //获取所有包含可以作为拼图图片的文件夹的路径并保存到集合中
        initData();
        //设置底部点击事件,弹出PopupWindow选择文件夹
        initEvent();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setBackgroundDrawable(null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        View customView = LayoutInflater.from(this).inflate(R.layout.picture_seletor_actionbar, null);
        mBack = (ImageButton) customView.findViewById(R.id.id_actionbar_back);
        mTitle = (TextView) customView.findViewById(R.id.id_actionbar_title);
        mPreview = (ImageButton) customView.findViewById(R.id.id_actionbar_preview);
        mChoice = (ImageButton) customView.findViewById(R.id.id_actionbar_choice);
        mDelete = (ImageButton) customView.findViewById(R.id.id_actionbar_delete);
        mEditView = (RelativeLayout) customView.findViewById(R.id.id_actionbar_editView);
        actionBar.setCustomView(customView);
        Toolbar parent = (Toolbar) customView.getParent();
        //v7包需要手动设置view填满actionbar左右两边，布局中最外层容器height必须为wrap_content
        parent.setContentInsetsAbsolute(0, 0);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTotalDeleteCount > 0) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                }
                ImageLoaderActivity.this.finish();
            }
        });
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagesPathsBean imagePaths = new ImagesPathsBean(mSelectedPicsPaths);
                Intent intent = new Intent(ImageLoaderActivity.this, PreviewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("imagesPaths", imagePaths);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        mChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelectedPictures();
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialogPopupWindow alertDialog = new AlertDialogPopupWindow(ImageLoaderActivity.this);
                alertDialog.setNeutralButton("取消", new AlertDialogPopupWindow.OnClickListener() {
                    @Override
                    public void onClick() {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setPositiveButton("确定", new AlertDialogPopupWindow.OnClickListener() {
                    @Override
                    public void onClick() {
                        int count = 0;
                        for (String filePath : mSelectedPicsPaths) {
                            File file = new File(filePath);
                            if (file.exists() && file.isFile()) {
                                boolean isDelete = file.delete();
                                if (isDelete) {
                                    count++;
                                }
                            }
                        }
                        mTotalDeleteCount += count;
                        if (count > 0) {
                            if (count == mSelectedPicsPaths.size()) {
                                Toast.makeText(ImageLoaderActivity.this, "已删除" + count + "张图片", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ImageLoaderActivity.this, "已删除" + count + "张图片, 有" + (mSelectedPicsPaths.size() - count) + "张图片未删除或者所选图片不存在", Toast.LENGTH_SHORT).show();
                            }
                            //更新popupWindow的数据
                            mFolderBeans.get(mCurrentDirPosition).setImageCount(mCurrentDirPicsCount - count);
                            initDirsPopupWindow();
                            //更新底部信息栏的数据
                            mCurrentDirPicsCount -= count;
                            mProgressDialog = ProgressDialog.show(ImageLoaderActivity.this, null, "正在加载...");
                            showPics();
                            mProgressDialog.dismiss();
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(ImageLoaderActivity.this, "所选图片不存在或已经被删除", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                alertDialog.setTitle("警告！");
                alertDialog.setContent("此操作将会从手机中删除所选图片，是否继续本次操作？");
                alertDialog.addPicture(R.mipmap.shock);
                alertDialog.show();
            }
        });
    }

    private void setStateBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.actionbar);//通知栏所需颜色
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


    private void initView() {
        mGridView = (GridView) findViewById(R.id.gridView);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mBottomLayout.setVisibility(View.INVISIBLE);
        mDirName = (TextView) findViewById(R.id.dirName);
        mPhotoNum = (TextView) findViewById(R.id.photoNum);
    }

    private void initEvent() {
        mBottomLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //设置PopupWindow显示位置
                mPopupWindow.showAsDropDown(mBottomLayout, 0, 0);
                //设置PopupWindow显示动画
                mPopupWindow.setAnimationStyle(R.style.PopupWindow_Animation);
                //让内容背景变暗
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 0.3f;
                getWindow().setAttributes(params);
            }
        });
    }


    private void initData() {
        mSelectedPicsPaths = new HashSet<>();
        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
        //清空文件夹集合
        mFolderBeans.clear();
        //开启线程读取数据库
        new Thread() {
            public void run() {
                //指定需要查找的资源
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                //获得内容提供者
                ContentResolver contentResolver = ImageLoaderActivity.this
                        .getContentResolver();
                //根据图片修改日期查询,获得游标
                Cursor cursor = contentResolver.query(mImgUri, null,
                        MediaStore.Images.Media.MIME_TYPE + " = ? or "
                                + MediaStore.Images.Media.MIME_TYPE + " = ? ",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);
                Set<String> parentDir = new HashSet<>();
                //
                boolean currentDirChosen = false;
                int dirPosition = -1;
                //遍历资源游标
                while (cursor.moveToNext()) {
                    //图片路径
                    String path = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    //过滤不适合作为拼图的小图片
                    File file = new File(path);
                    if (file.length() < 1024 * 100)
                        continue;
                    //由于安全问题有些文件夹无法直接访问,跳过该文件夹
                    File parentFile = file.getParentFile();
                    if (parentFile == null)
                        continue;
                    //有些特殊文件由于权限原因致使list判空,跳过该文件夹
                    if (parentFile.list() == null) {
                        continue;
                    }
                    //获得图片所在的文件夹的路径
                    String parentPath = parentFile.getAbsolutePath();
                    FolderBean folderBean;
                    //当前文件夹已经扫描过则继续遍历游标
                    if (parentDir.contains(parentPath)) {
                        continue;
                    }
                    //将包含图片的文件夹添加到集合中保存
                    else {
                        parentDir.add(parentPath);
                        //将文件夹的路径和第一张显示的图片的路径封装
                        folderBean = new FolderBean();
                        folderBean.setDir(parentPath);
                        folderBean.setFirstImgPath(path);
                    }
                    //筛选过滤出不适合作为拼图的图片
                    filterImageNamesInDir(parentFile);

                    //最终获得文件夹中真正适合作为拼图资源的图片的数量
                    int picCount = mImagesNames.size();
                    //将图片数量一并封装
                    folderBean.setImageCount(picCount);
                    //添加文件Bean到集合中管理
                    mFolderBeans.add(folderBean);
                    dirPosition++;
                    //获得手机相册的路径
                    String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
                    //如果是首次初始化(先查找是否有相册目录(如果相册没有图片则没有改目录),如果有就选择图片最多的目录为当前目录)
                    if (mGoBackDir == null) {
                        //选择相册目录为当前目录
                        if (DCIM != null && parentPath.equals(DCIM)) {
                            //选择相册目录为当前文件夹
                            mCurrentDir = parentFile;
                            mCurrentDirPicsCount = picCount;
                            currentDirChosen = true;
                            mCurrentDirPosition = dirPosition;
                        }
                        //如果相册目录不存在则选择图片数量最多的文件夹为当前文件夹
                        else if (!currentDirChosen && picCount > mCurrentDirPicsCount) {
                            mCurrentDirPicsCount = picCount;
                            mCurrentDir = parentFile;
                            mCurrentDirPosition = dirPosition;
                        }
                    }
                    //如果是从其他Activity返回刚才打开的文件夹，则继续显示刚才打开的文件夹
                    else {
                        mCurrentDir = mGoBackDir;
                        mCurrentDirPicsCount = mGoBackDirPicsCount;
                    }
                }
                cursor.close();
                mHandler.sendEmptyMessage(PICS_LOADED);
            }
        }.start();
    }

    private void filterImageNamesInDir(File parentFile) {
        //首先过滤非图片格式文件
        mImagesNames = Arrays.asList(parentFile.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg");
            }
        }));
        //Arrays.asList() 返回java.util.Arrays$ArrayList， 而不是ArrayList。
        //Arrays$ArrayList和ArrayList都是继承AbstractList，remove，add等 method在AbstractList中是默认throw UnsupportedOperationException而且不作任何操作。
        //ArrayList override这些method来对list进行操作，但是Arrays$ArrayList没有override remove(int)，add(int)等，所以throw UnsupportedOperationException。
        //解决方法是使用Iterator，或者转换为ArrayList,这里先将其转换成ArrayList
        mImagesNames = new ArrayList<>(mImagesNames);
        String dir = parentFile.getAbsolutePath();
        //再将图片大小太小不适合作为拼图的图片过滤
        for (int i = 0, s = mImagesNames.size(); i < s; i++) {
            String name = mImagesNames.get(i);
            File tempFile = new File(dir, name);
            if (tempFile.length() < 1024 * 100) {
                mImagesNames.remove(name);
                s--;
                i--;
            }
        }
    }

    //根据当前文件夹路径显示可以作为拼图的图片
    private void showPics() {
        if (mCurrentDir == null) {
            Toast.makeText(this, "未扫描到任何图片", Toast.LENGTH_SHORT).show();
            mPhotoNum.setText(mCurrentDirPicsCount + "张");
            mDirName.setText("未找到图片");
            return;
        }
        //清空已选择的图片路径
        mSelectedPicsPaths.clear();
        //显示标题
        mTitle.setVisibility(View.VISIBLE);
        //隐藏编辑栏
        mEditView.setVisibility(View.INVISIBLE);
        filterImageNamesInDir(mCurrentDir);
        mImageAdapter = new ImageAdapter(this, mImagesNames, mCurrentDir.getAbsolutePath());
        mImageAdapter.setOnImageChoiceListener(this);
        mGridView.setAdapter(mImageAdapter);
        mPhotoNum.setText(mCurrentDirPicsCount + "张");
        mDirName.setText(mCurrentDir.getName());
    }

    private void initDirsPopupWindow() {
        mPopupWindow = new DirListPopupWindow(mFolderBeans, this);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                //重新将内容区域变亮
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });
        //文件夹列表的点击事件
        mPopupWindow.setOnDirSelectedListener(new DirListPopupWindow.OnDirSelectedListener() {

            @Override
            public void onSelected(FolderBean folderBean, int position) {
                mCurrentDir = new File(folderBean.getDir());
                mCurrentDirPosition = position;
                mCurrentDirPicsCount = folderBean.getImageCount();
                filterImageNamesInDir(mCurrentDir);
                mSelectedPicsPaths.clear();
                mTitle.setVisibility(View.VISIBLE);
                mEditView.setVisibility(View.INVISIBLE);
                mImageAdapter = new ImageAdapter(ImageLoaderActivity.this, mImagesNames, folderBean.getDir());
                mImageAdapter.setOnImageChoiceListener(ImageLoaderActivity.this);
                mGridView.setAdapter(mImageAdapter);
//                mImageAdapter.notifyDataSetChanged();
                mDirName.setText(folderBean.getName());
                mPhotoNum.setText(mImagesNames.size() + "张");
                mPopupWindow.dismiss();
            }
        });
    }

    private void saveSelectedPictures() {
        SharedPreferences sharedPreferences = getSharedPreferences("imagePaths", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> filePaths = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            filePaths = sharedPreferences.getStringSet("imagePaths", new HashSet<String>());
        }
        Iterator<String> it = mSelectedPicsPaths.iterator();
        while (it.hasNext()) {
            String temp = it.next();
            if (!filePaths.contains(temp))
                filePaths.add(temp);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor.clear();
            editor.commit();
            editor.putStringSet("imagePaths", filePaths);
            editor.commit();
        }
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        this.finish();
    }

    @Override
    public void imageChoice(Set<String> selectedPaths) {
        mSelectedPicsPaths = selectedPaths;
        if (selectedPaths.size() > 0) {
            mEditView.setVisibility(View.VISIBLE);
            mTitle.setVisibility(View.INVISIBLE);
        } else {
            mEditView.setVisibility(View.INVISIBLE);
            mTitle.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == 0x33) {
                Bundle bundle = data.getExtras();
                int deleteCount = bundle.getInt("deleteCount");
                if (deleteCount > 0) {
                    //更新popupWindow的数据
                    mFolderBeans.get(mCurrentDirPosition).setImageCount(mCurrentDirPicsCount - deleteCount);
                    initDirsPopupWindow();
                    //更新底部信息栏的数据
                    mCurrentDirPicsCount -= deleteCount;
                    mProgressDialog = ProgressDialog.show(ImageLoaderActivity.this, null, "正在加载...");
                    showPics();
                    mProgressDialog.dismiss();
                }
            }
            if (resultCode == 0x44) {
                mGoBackDir = mCurrentDir;
                mGoBackDirPicsCount = mCurrentDirPicsCount;
                Toast.makeText(ImageLoaderActivity.this, "添加了新的图片，重新加载数据...", Toast.LENGTH_LONG).show();
                initData();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
