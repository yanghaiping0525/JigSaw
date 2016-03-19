package com.yang.jigsaw.view;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.yang.jigsaw.R;
import com.yang.jigsaw.bean.ImagePiece;
import com.yang.jigsaw.utils.ImageLoader;
import com.yang.jigsaw.utils.ImageSplitterUtil;
import com.yang.jigsaw.utils.ScreenSize;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

public class JigsawLayout extends RelativeLayout implements OnClickListener {
    //拼图分割的列数,默认3列
    private int mColumn = 3;
    //整幅拼图距离屏幕周边的距离
    private int mPadding;
    //拼图每个块之间的间隙
    private int mMargin = 3;
    //所有拼图块数组集合
    private ImageView[] piecesSort;
    //每个小拼图块的宽度
    private int pieceWidth;
    //当前拼图的Bitmap
    private Bitmap bitmapBg;
    //所有经过分割后的拼图块集合
    private List<ImagePiece> imagePieces;
    private boolean once;
    //整幅拼图的宽度
    private int jigsawWidth;
    //手指点击的第一个拼图块和第二个拼图块
    private ImageView mFirstPiece, mSecondPiece;
    //模拟拼图块之间交换移动的动画层
    private RelativeLayout mAnimLayout;
    //拼图块正在移动和交换的标志
    private boolean isExchanging;
    //供外界调用拼图事件的接口(通关事件,下一关事件,通关失败事件)
    private JigsawListener mListener;
    //是否计时的标志
    private boolean isTimeEnable = false;
    //通关标志
    private boolean isPass;
    //结束标志
    private boolean isOver;
    //关卡关数
    public int level = 1;
    //计时的时间
    private int runningTime;
    //游戏暂停的标志
    private boolean isPause;
    // public int maxLevelProgress = 1;
    //保存图片资源id
    private Integer[] resourceIds;
    private Context mContext;
    //游戏继续标志
    private boolean isContinue;
    //拼图宽高
    private int mScreenWidth, mScreenHeight;
    //配置中选择的所选图片、游戏时间大小
    private String mBackGroundPictures, mGameTime;
    //拼图分割的列数(默认3x3),可以在设置中改变该值
    private int mGameLayout;
    //游戏设计默认通关一个拼图后加载下一幅图,如果该值设置为2,则表示继续该图片,但是难度继续加大,那么同一幅图就玩了2次拼图，第二次的难度比第一次加大，以此类推,可以在设置中改变该值。
    private int mGameProgressive;
    //图库中自己已添加的图片的数量
    private int mMyOwnPhotoCount;

    public void setOnJigsawListener(JigsawListener listener) {
        this.mListener = listener;
    }

    private static final int TIME_CHANGE = 0x00;
    private static final int NEXT_LEVEL = 0x01;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TIME_CHANGE:
                    if (isPass || isOver || isPause) {
                        return;
                    }
                    if (mListener != null) {
                        //如果游戏失败,通知监听者完成相应操作,结束计时
                        if (runningTime < 0) {
                            isOver = true;
                            mListener.gameOver();
                            return;
                        }
                        //通知监听者刷新时间
                        mListener.timeChange(runningTime);
                    }
                    runningTime--;
                    //每一秒计时一次
                    mHandler.sendEmptyMessageDelayed(TIME_CHANGE, 1000);
                    break;

                case NEXT_LEVEL:
                    if (mListener != null) {
                        mListener.nextLevel(level);
                    } else {
                        nextLevel();
                    }
                    break;
            }
        }

    };

    public JigsawLayout(Context context) {
        this(context, null);
    }

    public JigsawLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public JigsawLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mScreenWidth = ScreenSize.getWidth(context);
        mScreenHeight = ScreenSize.getHeight(context);
        //初始化某些属性及资源
        initView();
        //读取游戏配置文件,初始化配置
        initSetting(context);
        //获得自定义图片数量
        calculateMyOwnPhotoCount();
    }

    //该view加载时会调用该方法,在该方法中完成拼图的分割与排序
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //初始化拼图宽度,为宽高的较小者
        jigsawWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
        if (!once) {
            //分割图片并乱序排列
            initPieces();
            //在布局中排列经过乱序排列后的拼图块
            initSortView();
            //判断是否需要计时
            checkIfTimeEnable();
            once = true;
        }
        //指定自定义view的大小为正方形，边长为拼图的宽度
        setMeasuredDimension(jigsawWidth, jigsawWidth);
    }

    @Override
    public void onClick(View piece) {
        if (!isExchanging) {
            // 重复点击第一个图片,将图片还原成原来的颜色
            if (mFirstPiece == piece) {
                mFirstPiece.setColorFilter(null);
                mFirstPiece = null;
                return;
            }
            //设置第一张点击的拼图块颜色
            if (mFirstPiece == null) {
                mFirstPiece = (ImageView) piece;
                mFirstPiece.setColorFilter(Color.parseColor("#55ff0000"));
            }
            //点击了第二张拼图块,进行拼图块之间的图片交换
            else {
                mSecondPiece = (ImageView) piece;
                //完成交换动画,交换两张被点击拼图的bitmap和index,并判断是否通关
                exchangePiecePosition();
            }
        }
    }

    //完成交换动画,交换两张被点击拼图的bitmap和index,并判断是否通关
    private void exchangePiecePosition() {
        isExchanging = true;
        //还原图片颜色
        mFirstPiece.setColorFilter(null);
        //准备动画层
        initAnimationLayout();
        //准备上层动画图片,复制点击的两张图片，添加到动画层,完成动画效果
        ImageView firstCopy = new ImageView(mContext);
        final String firstTag = (String) mFirstPiece.getTag();
        String[] firstParams = firstTag.split("_");
        //获得第一张被点击的拼图块的bitmap
        final Bitmap firstBitmap = imagePieces.get(
                Integer.parseInt(firstParams[0])).getBitmap();
        //将刚才获得的bitmap赋给动画层的ImageView,完成复制
        firstCopy.setImageBitmap(firstBitmap);
        //获得所需大小
        LayoutParams firstCopyParams = new LayoutParams(
                pieceWidth, pieceWidth);
        //获得位置
        firstCopyParams.leftMargin = mFirstPiece.getLeft() - mPadding;
        firstCopyParams.topMargin = mFirstPiece.getTop() - mPadding;
        firstCopy.setLayoutParams(firstCopyParams);
        //将拷贝完成的ImageView添加到动画层
        mAnimLayout.addView(firstCopy);
        //同理复制第二个点击的拼图块,添加到动画层
        ImageView secondCopy = new ImageView(mContext);
        final String secondTag = (String) mSecondPiece.getTag();
        String[] secondParams = secondTag.split("_");
        final Bitmap secondBitmap = imagePieces.get(
                Integer.parseInt(secondParams[0])).getBitmap();
        secondCopy.setImageBitmap(secondBitmap);
        LayoutParams secondCopyParams = new LayoutParams(
                pieceWidth, pieceWidth);
        secondCopyParams.leftMargin = mSecondPiece.getLeft() - mPadding;
        secondCopyParams.topMargin = mSecondPiece.getTop() - mPadding;
        secondCopy.setLayoutParams(secondCopyParams);
        mAnimLayout.addView(secondCopy);

        //设置第一个拼图块拷贝者的动画
        TranslateAnimation animFirst = new TranslateAnimation(0,
                mSecondPiece.getLeft() - mFirstPiece.getLeft(), 0,
                mSecondPiece.getTop() - mFirstPiece.getTop());
        animFirst.setDuration(300);
        animFirst.setFillAfter(true);
        firstCopy.startAnimation(animFirst);
        //设置第二个拼图块拷贝者的动画
        TranslateAnimation animSecond = new TranslateAnimation(0,
                mFirstPiece.getLeft() - mSecondPiece.getLeft(), 0,
                mFirstPiece.getTop() - mSecondPiece.getTop());
        animSecond.setDuration(300);
        animSecond.setFillAfter(true);
        secondCopy.startAnimation(animSecond);

        animFirst.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                //动画开始,隐藏原来被点击的两张拼图块
                mFirstPiece.setVisibility(View.INVISIBLE);
                mSecondPiece.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //动画结束,真正交换两张被点击的拼图的bitmap以及Tag,并显示出来
                mFirstPiece.setImageBitmap(secondBitmap);
                mSecondPiece.setImageBitmap(firstBitmap);
                mFirstPiece.setTag(secondTag);
                mSecondPiece.setTag(firstTag);
                mFirstPiece.setVisibility(View.VISIBLE);
                mSecondPiece.setVisibility(View.VISIBLE);
                //释放动画层资源
                mFirstPiece = mSecondPiece = null;
                mAnimLayout.removeAllViews();
                //完成交换后检查是否达到通关条件
                checkSuccess();
                isExchanging = false;

            }
        });
    }

    private void calculateMyOwnPhotoCount() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("imagePaths", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> filePaths = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            filePaths = sharedPreferences.getStringSet("imagePaths", new HashSet<String>());
        }
        int count = filePaths.size();
        int finalCount = 0;
        //判断是否有被删除的图片
        Iterator<String> iterator = filePaths.iterator();
        if (filePaths != null && count > 0) {
            for (int i = 0; i < count; i++) {
                File file = new File(iterator.next());
                if (file.exists()) {
                    finalCount++;
                } else {
                    iterator.remove();
                }
            }
        }
        //有被删除的图片，更新图片路径
        if (finalCount != count) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.clear();
                editor.commit();
                editor.putStringSet("imagePaths", filePaths);
                while (editor.commit()) {
                    break;
                }
            }
        }
        mMyOwnPhotoCount = finalCount;
    }

    //读取游戏配置文件,初始化配置
    private void initSetting(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("backgroundPictures", Context.MODE_PRIVATE);
        mBackGroundPictures = sharedPreferences.getString("backgroundPictures", "defaultPictures");
        sharedPreferences = context.getSharedPreferences("gameLayout", Context.MODE_PRIVATE);
        mColumn = mGameLayout = sharedPreferences.getInt("gameLayout", 3);
        sharedPreferences = context.getSharedPreferences("gameTime", Context.MODE_PRIVATE);
        mGameTime = sharedPreferences.getString("gameTime", "normal");
        sharedPreferences = context.getSharedPreferences("gameProgressive", Context.MODE_PRIVATE);
        mGameProgressive = sharedPreferences.getInt("gameProgressive", 1);
    }


    private void initView() {
        //将px值转换成dp值
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mMargin, getResources().getDisplayMetrics());
        //获得padding值
        mPadding = min(getPaddingBottom(), getPaddingTop(), getPaddingLeft(),
                getPaddingRight());
        //默认4张图片的资源id
        resourceIds = new Integer[4];
        int resourceLength = resourceIds.length;
        for (int i = 0; i < resourceLength; i++) {
            resourceIds[i] = R.mipmap.bg_1 + i;
        }
        //乱序排序
        Collections.shuffle(Arrays.asList(resourceIds));//resourceIds如果是int[]无法进行随机排序

    }

    private int min(int... params) {
        int min = 0;
        if (params != null && params.length != 0) {
            min = params[0];
            for (int param : params) {
                if (param < min) {
                    min = param;
                }
            }
        }
        return min;
    }


    private void checkIfTimeEnable() {
        if (isTimeEnable) {
            //先获得游戏每一关的时间
            countTimeBaseLevelAndSetting();
        }
    }

    private void countTimeBaseLevelAndSetting() {
        //根据获得的配置信息计算每一关的通关时间
        if (mGameTime.equals("short")) {
            runningTime = (int) Math.pow(1.5, (level - 1) % mGameProgressive + 1) * 30;
        } else if (mGameTime.equals("normal")) {
            runningTime = (int) Math.pow(1.5, (level - 1) % mGameProgressive + 1) * 60;
        } else if (mGameTime.equals("long")) {
            runningTime = (int) Math.pow(1.5, (level - 1) % mGameProgressive + 1) * 120;
        } else {
            throw new RuntimeException("时间初始化失败");
        }
        //开始计时
        mHandler.sendEmptyMessage(TIME_CHANGE);
    }

    private void initSortView() {
        //计算每一块小拼图的宽度，用于布局
        pieceWidth = (jigsawWidth - mPadding * 2 - mMargin * (mColumn - 1))
                / mColumn;
        //所有拼图块数组集合
        piecesSort = new ImageView[mColumn * mColumn];
        //在布局中排列所有拼图块
        for (int i = 0; i < piecesSort.length; i++) {
            ImageView item = new ImageView(mContext);
            //设置点击事件
            item.setOnClickListener(this);
            item.setImageBitmap(imagePieces.get(i).getBitmap());
            //设置id用于布局排列位置
            item.setId(i + 1000);
            //设置Tag用于处理通关逻辑(i用于获得对应的bitmap,index用于判断通关逻辑)
            item.setTag(i + "_" + imagePieces.get(i).getIndex());
            piecesSort[i] = item;
            LayoutParams params = new LayoutParams(
                    pieceWidth, pieceWidth);
            // 设置piece间横向间距(最后一列没有右边距)
            if ((i + 1) % mColumn != 0) {
                params.rightMargin = mMargin;
            }
            // 设置每一列位置(除了第一列，每一列都要设置right_of属性)
            if (i % mColumn != 0) {
                params.addRule(RelativeLayout.RIGHT_OF,
                        piecesSort[i - 1].getId());
            }
            // 设置piece间纵向间距(第一行没有上边距)
            if ((i + 1) > mColumn) {
                params.topMargin = mMargin;
                params.addRule(RelativeLayout.BELOW,
                        piecesSort[i - mColumn].getId());
            }
            addView(item, params);
        }
    }

    private void initPieces() {
        //如果通关后没有选择继续该拼图,则重新加载一张图片
        if (!isContinue) {
            if (((level - 1) % mGameProgressive) == 0) {
                //读取配置文件,判断需要加载的图片资源
                //只加载默认提供的图片
                if (mBackGroundPictures.equals("defaultPictures")) {
                    initImageBackgroundFromResource();
                }
                //只加载自己选择的图库的图片
                else if (mBackGroundPictures.equals("myPictures")) {
                    initImageBackgroundFromFile();
                }
                //加载所有图片，每个图片被选中的概率一样
                else {
                    if (mMyOwnPhotoCount > 0) {
                        //默认图片4张,被选中
                        if (Math.random() < (4 / (float) (4 + mMyOwnPhotoCount))) {
                            initImageBackgroundFromResource();
                        }
                        //自定义图片被选中
                        else {
                            initImageBackgroundFromFile();
                        }
                    }
                    //如果没有添加自定义的图片,默认加载推荐图片
                    else {
                        initImageBackgroundFromResource();
                    }
                }
            }
        }
        isContinue = false;
        //开始根据需要分割的列数和选定的图片来分割图片
        if (bitmapBg != null) {
            //得到分割后的拼图块
            imagePieces = ImageSplitterUtil.splitImage(bitmapBg, mColumn);
        } else {
            throw new RuntimeException("找不到拼图");
        }
        //判断分割排列后是否和原来拼图一样,如果是则需要重新排列,直到和原来拼图不一样
        boolean success = true;
        while (success) {
            //将拼图块随机排序
            Collections.sort(imagePieces, new Comparator<ImagePiece>() {

                @Override
                public int compare(ImagePiece lhs, ImagePiece rhs) {

                    return Math.random() > 0.5 ? 1 : -1;
                    // return 0 两者相等 return正数表示第一个对象比第二个对象大�
                }

            });
            //如果有拼图的index和原始顺序不一致,才认为是乱序排序,退出循环
            for (int i = 0, len = imagePieces.size(); i < len; i++) {
                if (imagePieces.get(i).getIndex() != i) {
                    success = false;
                }
            }
        }

    }

    //从默认图片中选择拼图背景图片
    private void initImageBackgroundFromResource() {
        //循环选择乱序排列的默认图片
        int i = level % 4;
        bitmapBg = BitmapFactory.decodeResource(getResources(),
                resourceIds[i]);
    }

    //根据图片路径从自定义图片的配置文件中读取图片
    private void initImageBackgroundFromFile() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("imagePaths", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> filePaths = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            filePaths = sharedPreferences.getStringSet("imagePaths", new HashSet<String>());
        }
        int count = filePaths.size();
        int finalCount = 0;
        //判断是否有被删除的图片
        Iterator<String> iterator = filePaths.iterator();
        if (filePaths != null && count > 0) {
            for (int i = 0; i < count; i++) {
                File file = new File(iterator.next());
                if (file.exists()) {
                    finalCount++;
                } else {
                    iterator.remove();
                }
            }
        }
        //有被删除的图片，更新图片路径
        if (finalCount != count) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.clear();
                editor.commit();
                editor.putStringSet("imagePaths", filePaths);
                while (editor.commit()) {
                    break;
                }
            }
        }
        //如果自定义图片有效数量不为0
        if (finalCount > 0) {
            iterator = filePaths.iterator();
            Random random = new Random();
            String[] paths = new String[finalCount];
            for (int i = 0; i < finalCount; i++) {
                paths[i] = iterator.next();
            }
            //bitmapBg = BitmapFactory.decodeFile(paths[random.nextInt(count)]);
            bitmapBg = ImageLoader.getInstance().decodeBitmapFromPath(paths[random.nextInt(finalCount)], mScreenWidth, mScreenHeight);
        }
        //自定义图片全部无效,选择默认图片
        else {
            initImageBackgroundFromResource();
        }
    }


    private void initAnimationLayout() {
        if (mAnimLayout == null) {
            mAnimLayout = new RelativeLayout(mContext);
            addView(mAnimLayout);
        }
    }

    private void checkSuccess() {
        boolean isSuccess = true;
        for (int i = 0; i < piecesSort.length; i++) {
            ImageView piece = piecesSort[i];
            String tag = (String) piece.getTag();
            String[] params = tag.split("_");
            //params[1]即index值，为分割时候按位置顺序分配的数字,为自然顺序
            //如果有一个index值不等于自然顺序,则未完成
            if (Integer.parseInt(params[1]) != i) {
                isSuccess = false;
            }
        }
        //通关成功
        if (isSuccess) {
            //清除计时消息,若不清空,下次计时的速度将是多个消息的效果累加
            mHandler.removeMessages(TIME_CHANGE);
            isPass = true;
            //通知监听者执行通关逻辑
            mHandler.sendEmptyMessage(NEXT_LEVEL);
        }
    }

    public void nextLevel() {
        this.removeAllViews();
        mAnimLayout = null;
        level++;
//        if (level > maxLevelProgress) {
//            maxLevelProgress = level;
//        }
        mColumn++;
        mColumn = (mColumn - mGameLayout) % mGameProgressive + mGameLayout;
        isPass = false;
        initPieces();
        initSortView();
        checkIfTimeEnable();
    }

    public void continueCurrentLevel() {
        mHandler.removeMessages(TIME_CHANGE);
        this.removeAllViews();
        isOver = false;
        mAnimLayout = null;
        isPass = false;
        isContinue = true;
        initPieces();
        initSortView();
        checkIfTimeEnable();
    }

    public boolean isPause() {
        return isPause;
    }


    public void pause() {
        isPause = true;
        mHandler.removeMessages(TIME_CHANGE);
    }

    public void resume() {
        if (isPause) {
            isPause = false;
            mHandler.sendEmptyMessage(TIME_CHANGE);
        }
    }

    public void reload() {
        resetLevel();
        isOver = false;
        isPause = false;
        if (!isTimeEnable) {
            mHandler.removeMessages(TIME_CHANGE);
        }
        //读取配置信息
        initSetting(mContext);
        //移除当前的拼图
        this.removeAllViews();
        //重置动画层
        mAnimLayout = null;
        //分割图片并乱序排列
        initPieces();
        //在布局中排列经过乱序排列后的拼图块
        initSortView();
        //判断是否需要计时
        checkIfTimeEnable();
    }

    public void resetLevel() {
        level = 1;
    }

    public void setTimeEnable(boolean isTimeEnable) {
        this.isTimeEnable = isTimeEnable;
    }

    public interface JigsawListener {
        void nextLevel(int level);

        void timeChange(int currentTime);

        void gameOver();
    }

    public boolean checkIsTimeEnable() {
        return isTimeEnable;
    }

}
