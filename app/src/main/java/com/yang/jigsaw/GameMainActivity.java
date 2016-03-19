package com.yang.jigsaw;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yang.jigsaw.adapter.SongAdapter;
import com.yang.jigsaw.bean.SongBean;
import com.yang.jigsaw.fragment.JigsawFragment;
import com.yang.jigsaw.fragment.PhotoFragment;
import com.yang.jigsaw.fragment.SettingFragment;
import com.yang.jigsaw.service.SongService;
import com.yang.jigsaw.utils.ActionBarHelper;
import com.yang.jigsaw.utils.ActivityStateHelper;
import com.yang.jigsaw.utils.StringMatcher;
import com.yang.jigsaw.utils.SystemBarTintManager;
import com.yang.jigsaw.view.AlertDialogPopupWindow;
import com.yang.jigsaw.view.IndexAbleListView;
import com.yang.jigsaw.view.SlideAbleView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by YangHaiPing on 2016/2/25.
 */
public class GameMainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, Runnable, SongService.OnSongCompleteListener {
    private RadioGroup mRadioGroup;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    //拼图Fragment
    private JigsawFragment jigsawFragment;
    //图库Fragment
    private PhotoFragment photoFragment;
    //设置Fragment
    private SettingFragment settingFragment;
    private ActionBar mActionBar;
    //拼图、图库、设置界面自定义不同的ActionBar的View
    private View customView_main, customView_photo, customView_setting;
    //自定义带有侧拉菜单栏的view
    private SlideAbleView mSlideView;
    //带有字母索引的ListView显示歌曲
    private IndexAbleListView mSongList;
    //歌曲播放界面的按钮,上一首、播放、暂停、下一首
    private ImageButton mPrevious, mPlay, mPause, mNext;
    //当前播放的歌曲在歌曲列表中的位置
    private int mCurrentPosition = -1;
    //歌曲初始化是否完成标志
    private boolean mMusicReady = false;
    //上一次播放歌曲在歌曲列表中的位置
    private int mLastClickPosition = -1;
    //媒体资源是否释放标志
    private boolean isRelease = false;
    private static final int TIME_CHANGE = 0x11;
    //歌曲播放进度条
    private SeekBar mSongSeekBar;
    //歌曲的播放的时间以及歌曲时长
    private TextView mRunningTime, mDuration;
    //自定义歌曲适配器
    private SongAdapter mAdapter;
    //音乐播放模式的显示文字
    private TextView mPlayModeText;
    //音乐播放模式的显示图标
    private ImageView mHeaderIcon;
    //本地歌曲信息List
    private List<SongBean> mSongsInfo = new ArrayList<>();
    //音乐播放模式
    private static int mPlayMode = -1;
    //音乐播放模式之列表播放、循环播放、随机播放
    private final static int LIST_MODE = 0, LOOP_MODE = 1, RANDOM_MODE = 2;
    //用于更新歌曲播放时间的ExecutorService
    private ExecutorService mTimeChangeEs = Executors.newSingleThreadExecutor();
    //用于绑定音乐后台服务的Binder
    private SongService.SongBinder mSongBinder;
    //自定义音乐后台服务
    private SongService mSongService;
    //是否绑定音乐服务的标志
    private boolean isBind;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private Notification.Builder mBuilder;
    //当前显示的Fragment
    private static int mCurrentFragmentIndex = -1;
    //Fragment索引
    private static final int GAME_FRAGMENT = 0;
    private static final int PHOTO_FRAGMENT = 1;
    private static final int SETTING_FRAGMENT = 2;
    private static final String ACTION_PLAY = "com.yang.jigsaw.broadcast.player.play";
    private static final String ACTION_PREVIOUS = "com.yang.jigsaw.broadcast.player.previous";
    private static final String ACTION_NEXT = "com.yang.jigsaw.broadcast.player.next";

    //监听Home键事件的广播接受者
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) && mCurrentPosition >= 0) {
                if (TextUtils.equals(intent.getStringExtra(SYSTEM_REASON), SYSTEM_HOME_KEY)) {
                    RemoteViews contentView = getRemoteViews();
                    if (mSongService != null && mSongService.isPlaying()) {
                        contentView.setImageViewResource(R.id.id_statusBar_play, R.mipmap.pause_gray_icon_small);
                    } else {
                        contentView.setImageViewResource(R.id.id_statusBar_play, R.mipmap.play_gray_icon);
                    }
                    SongBean songBean = mSongsInfo.get(mCurrentPosition - 1);
                    contentView.setTextViewText(R.id.id_statusBar_song_name, songBean.getName());
                    contentView.setTextViewText(R.id.id_statusBar_singer, songBean.getSinger());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mNotificationManager.notify(0, mBuilder.build());
                    } else {
                        mNotificationManager.notify(0, mNotification);
                    }
                } else if (TextUtils.equals(intent.getStringExtra(SYSTEM_REASON), SYSTEM_HOME_KEY_LONG)) {
                }

            }
        }
    };

    //监听播放事件的广播接受者
    private BroadcastReceiver mSongPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mSongService != null && mSongService.isPlaying()) {
                pauseSongIfCanBe();
            } else {
                playSongIfCanBe();
            }
        }
    };

    //监听下一首播放事件的广播接受者
    private BroadcastReceiver mSongNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playNextSong();
        }
    };

    //监听上一首播放事件的广播接受者
    private BroadcastReceiver mSongPreviousReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playPreviousSong();
        }
    };

    //自定义handler用于更新歌曲播放时间
    private Handler mSongHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIME_CHANGE) {
                mRunningTime.setText(msg.obj.toString());
            }
        }
    };

    //绑定音乐服务的纽带
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSongBinder = (SongService.SongBinder) service;
            mSongService = mSongBinder.getService();
            mSongService.setOnSongCompleteListener(GameMainActivity.this);
            mSongService.initMediaPlayer();
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mSlideView.hideSlideView();
        //绑定音乐播放服务
        if (!isBind) {
            Intent intent = new Intent(GameMainActivity.this, SongService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNotificationManager.cancelAll();
        //如果拼图在计时模式且没有结束则继续开始
        if (jigsawFragment.isPause() && jigsawFragment.isCounting()) {
            jigsawFragment.resume();
        }
        //恢复歌曲计时功能
        if (mMusicReady) {
            if (mSongService != null && mSongService.isPlayerInitFinish()) {
                mTimeChangeEs.execute(GameMainActivity.this);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_main_activity);
        //初始化控件
        initFindViewById();
        //初始化自定义actionBar
        initActionBar();
        //统一状态栏和actionBar的颜色
        initStateBar();
        //初始化Fragment
        initFragment();
        //初始化歌曲列表
        searchSongs();
        //显示歌曲列表
        showSongList();
        //初始化音乐播放器按键事件
        initSongEvent();
        //注册广播接收者
        initBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停计时模式的拼图
        if (!jigsawFragment.isPause()) {
            jigsawFragment.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清除所有通知
        mNotificationManager.cancelAll();
        //解除所有广播接收
        if (mSongPlayReceiver != null) {
            unregisterReceiver(mSongPlayReceiver);
        }
        if (mSongNextReceiver != null) {
            unregisterReceiver(mSongNextReceiver);
        }
        if (mSongPreviousReceiver != null) {
            unregisterReceiver(mSongPreviousReceiver);
        }
        if (mHomeKeyEventReceiver != null) {
            unregisterReceiver(mHomeKeyEventReceiver);
        }
        //释放媒体资源
        mSongService.release();
        isRelease = true;
        //解除与音乐服务的绑定
        if (isBind) {
            unbindService(conn);
            isBind = false;
        }
        mSongService = null;
    }

    //因为嵌套的Fragment无法接收到onActivityResult事件，所以需要重写该方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FragmentManager fm = getSupportFragmentManager();
        int index = requestCode >> 16;
        if (index != 0) {
            index--;
            if (fm.getFragments() == null || index < 0
                    || index >= fm.getFragments().size()) {
                return;
            }
            Fragment fragment = fm.getFragments().get(index);
            if (fragment != null) {
                //递归传递result给嵌套的Fragment
                handleResult(fragment, requestCode, resultCode, data);
            }
            return;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //监听返回按钮事件
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            final AlertDialogPopupWindow alertDialog = new AlertDialogPopupWindow(this);
            alertDialog.setTitle("提示");
            alertDialog.setContent("即将退出游戏");
            alertDialog.addPicture(R.mipmap.miss);
            alertDialog.setPositiveButton("确定", new AlertDialogPopupWindow.OnClickListener() {
                @Override
                public void onClick() {
                    alertDialog.dismiss();
                    GameMainActivity.this.finish();
                }
            });
            alertDialog.setNegativeButton("取消", new AlertDialogPopupWindow.OnClickListener() {
                @Override
                public void onClick() {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
            return true;
        }
        return false;
    }

    //利用RadioGroup的多选一性质，显示被选中的Fragment
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mFragmentTransaction = mFragmentManager.beginTransaction();
        switch (checkedId) {
            //如果点击了游戏radioButton
            case R.id.id_fragment_main: {
                //更换为游戏主界面的ActionBar
                changeToGameActionBar();
                switch (mCurrentFragmentIndex) {
                    case PHOTO_FRAGMENT:
                        if (!jigsawFragment.isAdded()) {
                            mFragmentTransaction.add(R.id.id_fragment_content, jigsawFragment, jigsawFragment.getClass().getSimpleName()).hide(photoFragment);
                        } else {
                            mFragmentTransaction.show(jigsawFragment).hide(photoFragment);
                        }
                        break;
                    case SETTING_FRAGMENT:
                        if (!jigsawFragment.isAdded()) {
                            mFragmentTransaction.add(R.id.id_fragment_content, jigsawFragment, jigsawFragment.getClass().getSimpleName()).hide(settingFragment);
                        } else {
                            mFragmentTransaction.show(jigsawFragment).hide(settingFragment);
                        }
                        break;
                }
                if (jigsawFragment.isPause() && jigsawFragment.isCounting()) {
                    jigsawFragment.resume();
                }
                mCurrentFragmentIndex = GAME_FRAGMENT;
                break;
            }
            //如果点击了图库radioButton
            case R.id.id_fragment_photo: {
                //更换为图库界面的ActionBar
                changeToPhotoActionBar();
                switch (mCurrentFragmentIndex) {
                    case GAME_FRAGMENT:
                        if (!photoFragment.isAdded()) {
                            mFragmentTransaction.add(R.id.id_fragment_content, photoFragment, photoFragment.getClass().getSimpleName()).hide(jigsawFragment);
                        } else {
                            mFragmentTransaction.show(photoFragment).hide(jigsawFragment);
                        }
                        break;
                    case SETTING_FRAGMENT:
                        if (!photoFragment.isAdded()) {
                            mFragmentTransaction.add(R.id.id_fragment_content, photoFragment, photoFragment.getClass().getSimpleName()).hide(settingFragment);
                        } else {
                            mFragmentTransaction.show(photoFragment).hide(settingFragment);
                        }
                        break;
                }

                if (!jigsawFragment.isPause()) {
                    jigsawFragment.pause();
                }
                mCurrentFragmentIndex = PHOTO_FRAGMENT;
                break;
            }
            //如果点击了设置radioButton
            case R.id.id_fragment_setting: {
                //更换为设置界面的ActionBar
                changeToSettingActionBar();
                switch (mCurrentFragmentIndex) {
                    case GAME_FRAGMENT:
                        if (!settingFragment.isAdded()) {
                            mFragmentTransaction.hide(jigsawFragment).add(R.id.id_fragment_content, settingFragment, settingFragment.getClass().getSimpleName());
                        } else {
                            mFragmentTransaction.hide(jigsawFragment).show(settingFragment);
                        }
                        break;
                    case PHOTO_FRAGMENT:
                        if (!settingFragment.isAdded()) {
                            mFragmentTransaction.hide(photoFragment).add(R.id.id_fragment_content, settingFragment, settingFragment.getClass().getSimpleName());
                        } else {
                            mFragmentTransaction.hide(photoFragment).show(settingFragment);
                        }
                        break;
                }

                if (!jigsawFragment.isPause()) {
                    jigsawFragment.pause();
                }
                mCurrentFragmentIndex = SETTING_FRAGMENT;
                break;
            }
        }
        mFragmentTransaction.commit();
    }

    //监听音乐服务后台音乐的播放结束事件,此处响应下一首事件
    @Override
    public void onComplete() {
        playNextSong();
    }

    //此处为更新歌曲进度和歌曲播放时间的线程
    @Override
    public void run() {
        while (mSongService != null && !isRelease && mSongService.isPlaying()) {
            int currentPosition = mSongService.getCurrentPosition();
            if (currentPosition <= mSongSeekBar.getMax() && currentPosition != -1) {
                //更新seekBar的进度
                mSongSeekBar.setProgress(currentPosition);
                //每半秒刷新一次歌曲播放时间
                Message message = mSongHandler.obtainMessage(TIME_CHANGE, toTime(currentPosition));
                mSongHandler.sendMessageDelayed(message, 500);
            }
        }
    }

    //初始化控件
    private void initFindViewById() {
        mSlideView = (SlideAbleView) findViewById(R.id.id_slide_view);
        mRadioGroup = (RadioGroup) findViewById(R.id.id_radio_group);
        mRadioGroup.setOnCheckedChangeListener(this);
        mSongList = (IndexAbleListView) findViewById(R.id.id_music_list);
        mPrevious = (ImageButton) findViewById(R.id.id_player_last);
        mPlay = (ImageButton) findViewById(R.id.id_player_play);
        mPause = (ImageButton) findViewById(R.id.id_player_pause);
        mNext = (ImageButton) findViewById(R.id.id_player_next);
        mSongSeekBar = (SeekBar) findViewById(R.id.id_player_seekBar);
        mRunningTime = (TextView) findViewById(R.id.id_player_running_time);
        mDuration = (TextView) findViewById(R.id.id_player_duration);
    }

    //初始化歌曲列表
    private void searchSongs() {
        //获得内容提供者
        ContentResolver contentResolver = GameMainActivity.this
                .getContentResolver();
        //获得按默认顺序排列(字母顺序)的歌曲游标
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //遍历游标
        while (cursor.moveToNext()) {
            //歌曲ID
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            //歌曲名
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            //艺人
            String singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            if (singer.equals("<unknown>")) {
                singer = "未知艺人";
            }
            //专辑
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            if (album.equals("<unknown>")) {
                album = "未知专辑";
            }
            //歌曲路径
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            //文件名
            String display_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
            //年份
            String year = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
            //时长
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            //大小
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
            if (size < (2 << 17))//排除大小小于128K的音频文件
                continue;
            //封装歌曲信息
            SongBean songBean = new SongBean();
            songBean.setId(id);
            songBean.setName(name);
            songBean.setSinger(singer);
            songBean.setAlbum(album);
            songBean.setPath(path);
            songBean.setDisplay_name(display_name);
            songBean.setYear(year);
            songBean.setDuration(duration);
            songBean.setSize(size);
            //获得歌曲名字的首字母,用于索引
            songBean.setFirstLetter(StringMatcher.getFirstLetter(name.charAt(0)));
            //添加至歌曲list中
            mSongsInfo.add(songBean);
        }
        //释放游标资源
        cursor.close();
        mMusicReady = true;
    }

    //显示歌曲列表
    private void showSongList() {
        //初始化歌曲适配器
        mAdapter = new SongAdapter(this, mSongsInfo);
        //给歌曲listView添加头部view：必须放在setAdapter前面不然会报错
        View headView = LayoutInflater.from(this).inflate(R.layout.song_list_header, mSongList, false);
        mSongList.addHeaderView(headView, null, true);
        //隐藏head下面的分割线
        mSongList.setHeaderDividersEnabled(false);
        mPlayModeText = (TextView) headView.findViewById(R.id.id_song_header_play_mode);
        mHeaderIcon = (ImageView) headView.findViewById(R.id.id_song_header_icon);
        mSongList.setAdapter(mAdapter);
        mSongList.setVerticalScrollBarEnabled(false);
        mSongList.setFastScrollEnabled(true);
        //读取歌曲播放模式配置文件
        SharedPreferences sharedPreferences = getSharedPreferences("playMode", Context.MODE_PRIVATE);
        mPlayMode = sharedPreferences.getInt("mode", LIST_MODE);
        switch (mPlayMode) {
            case LIST_MODE:
                mHeaderIcon.setImageResource(R.mipmap.list_play);
                mPlayModeText.setText("顺序播放");
                break;
            case LOOP_MODE:
                mHeaderIcon.setImageResource(R.mipmap.loop_play);
                mPlayModeText.setText("循环播放");
                break;
            case RANDOM_MODE:
                mHeaderIcon.setImageResource(R.mipmap.random_play);
                mPlayModeText.setText("随机播放");
                break;
        }
        //添加歌曲Item的点击事件
        mSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //歌曲列表head(播放模式)被点击
                if (position == 0) {
                    if (mPlayModeText.getText().equals("随机播放")) {
                        SharedPreferences sharedPreferences = getSharedPreferences("playMode", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.commit();
                        editor.putInt("mode", LIST_MODE);
                        editor.commit();
                        mPlayMode = LIST_MODE;
                        mHeaderIcon.setImageResource(R.mipmap.list_play);
                        mPlayModeText.setText("顺序播放");
                    } else if (mPlayModeText.getText().equals("顺序播放")) {
                        SharedPreferences sharedPreferences = getSharedPreferences("playMode", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.commit();
                        editor.putInt("mode", LOOP_MODE);
                        editor.commit();
                        mPlayMode = LOOP_MODE;
                        mHeaderIcon.setImageResource(R.mipmap.loop_play);
                        mPlayModeText.setText("循环播放");
                    } else if (mPlayModeText.getText().equals("循环播放")) {
                        SharedPreferences sharedPreferences = getSharedPreferences("playMode", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.commit();
                        editor.putInt("mode", RANDOM_MODE);
                        editor.commit();
                        mPlayMode = RANDOM_MODE;
                        mHeaderIcon.setImageResource(R.mipmap.random_play);
                        mPlayModeText.setText("随机播放");
                    }
                }
                //某一首歌曲被点击
                else {
                    //获得被点击歌曲信息
                    SongBean songBean = mSongsInfo.get(position - 1);
                    //将被点击的位置传递给歌曲适配器，以便listView保存播放的歌曲目录位置(减一是因为ListView添加了一个head)
                    mAdapter.recordLastClickPosition(position - 1);
                    TextView name = (TextView) view.findViewById(R.id.id_song_name);
                    //如果ListView中未显示head(如果显示head的话，mSongList.getLastVisiblePosition() + head的数目 = mSongList.getChildCount())
                    if (mSongList.getLastVisiblePosition() + 1 != mSongList.getChildCount()) {
                        //如果之前有播放过歌曲,以及上一首歌不是现在播放的歌曲,则先将所有child的歌曲名字设置回黑色
                        if (mLastClickPosition != -1 && mLastClickPosition != position) {
                            for (int i = 0; i < mSongList.getChildCount(); i++) {
                                ((TextView) (parent.getChildAt(i).findViewById(R.id.id_song_name))).setTextColor(Color.BLACK);
                            }
                        }
                    }
                    //如果ListView中显示head
                    else {
                        //如果之前有播放过歌曲,以及上一首歌不是现在播放的歌曲,则将除了head以外的child中的歌曲名字置为黑色
                        if (mLastClickPosition != -1 && mLastClickPosition != position) {
                            for (int i = 1; i < mSongList.getChildCount(); i++) {
                                ((TextView) (parent.getChildAt(i).findViewById(R.id.id_song_name))).setTextColor(Color.BLACK);
                            }
                        }
                    }
                    //然后将所选歌曲的颜色置为红色
                    name.setTextColor(Color.RED);
                    //记录刚播放的歌曲目录的位置
                    mLastClickPosition = position;
                    //若上次播放的歌曲和要播放的歌曲不同则播放
                    if (mCurrentPosition != position) {
                        //先停止在播放的歌曲
                        if (mSongService.isPlaying()) {
                            mSongService.pause();
                        }
                        //初始化后台音乐服务,设置播放歌曲
                        mSongService.reset();
                        mSongService.setDataSource(songBean.getPath());
                        mSongService.prepare();
                        mSongService.start();
                        mPlay.setVisibility(View.GONE);
                        mPause.setVisibility(View.VISIBLE);
                        //记录现在播放的目录位置
                        mCurrentPosition = position;
                        int duration = mSongService.getDuration();
                        //该方法获得的并不准确
                        //mSongSeekBar.setMax(songBean.getDuration());
                        //重新设置进度条的最大值
                        mSongSeekBar.setMax(duration);
                        mDuration.setText(toTime(duration));
                        mTimeChangeEs.execute(GameMainActivity.this);
                        //发送广播通知
                        /*if (!ActivityStateHelper.isAppOnForeground(GameMainActivity.this)) {
                            RemoteViews contentView = getRemoteViews();
                            contentView.setTextViewText(R.id.id_statusBar_song_name, songBean.getName());
                            contentView.setTextViewText(R.id.id_statusBar_singer, songBean.getSinger());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                mNotificationManager.notify(0, mBuilder.build());
                            } else {
                                mNotificationManager.notify(0, mNotification);
                            }
                        }*/
                    }
                    //若点击的是上次播放的歌曲
                    else {
                        //如果歌曲还在播放中，则停止
                        if (mSongService.isPlaying()) {
                            mSongService.pause();
                            mPlay.setVisibility(View.VISIBLE);
                            mPause.setVisibility(View.GONE);
                        }
                        //如果歌曲已经停止则开始,并计算和刷新播放时间
                        else if (!mSongService.isPlaying()) {
                            mSongService.start();
                            mPlay.setVisibility(View.GONE);
                            mPause.setVisibility(View.VISIBLE);
                            mTimeChangeEs.execute(GameMainActivity.this);
                        }
                    }
                }
            }
        });

    }


    //初始化音乐播放器按键事件
    private void initSongEvent() {
        //播放下一首
        mPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousSong();
            }
        });

        //播放歌曲
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSongIfCanBe();
            }
        });

        //暂停歌曲
        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseSongIfCanBe();
            }
        });

        //播放下一首
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });
        //监听进度条拖动事件
        mSongSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //将音乐播放位置移至进度条拖动的位置
                    mSongService.seekTo(progress);
                    //重新更新拖动后的时间
                    mRunningTime.setText(toTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //注册广播接收者
    private void initBroadcastReceiver() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY);
        registerReceiver(mSongPlayReceiver, intentFilter);
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NEXT);
        registerReceiver(mSongNextReceiver, intentFilter);
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PREVIOUS);
        registerReceiver(mSongPreviousReceiver, intentFilter);
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeKeyEventReceiver, intentFilter);
    }

    //初始化Fragment
    private void initFragment() {
        mFragmentManager = getSupportFragmentManager();
        jigsawFragment = new JigsawFragment();
        photoFragment = new PhotoFragment();
        settingFragment = new SettingFragment();
        //首先显示游戏界面
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.id_fragment_content, jigsawFragment, jigsawFragment.getClass().getSimpleName());
        mFragmentTransaction.commit();
        mCurrentFragmentIndex = GAME_FRAGMENT;
    }

    //统一状态栏和actionBar的颜色(Android 4.4及以上有效)
    private void initStateBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.actionbar);//通知栏所需颜色
        }
    }

    //初始化自定义actionBar
    private void initActionBar() {
        mActionBar = getSupportActionBar();
        customView_main = LayoutInflater.from(this).inflate(R.layout.jigsaw_fragment_actionbar, null);
        customView_photo = LayoutInflater.from(this).inflate(R.layout.photo_fragment_actionbar, null);
        customView_setting = LayoutInflater.from(this).inflate(R.layout.setting_fragment_actionbar, null);
        mActionBar.setElevation(0);//消除分割线阴影，同时在主题中配置<item name="android:windowContentOverlay">@null</item>
        changeToGameActionBar();
    }


    private void playPreviousSong() {
        //如果播放模式是列表模式
        if (mPlayMode == LIST_MODE) {
            if (mMusicReady && mCurrentPosition != -1) {
                //>=1是由于添加了一个head
                if (mCurrentPosition - 1 >= 1) {
                    mCurrentPosition--;
                /*int index = mCurrentPosition - 1;
                if(mCurrentPosition <= mSongList.getFirstVisiblePosition()){
                    mSongList.smoothScrollToPosition(mSongList.getFirstVisiblePosition() - 1);
                }
                final long downTime = SystemClock.uptimeMillis();
                final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 0, 0, 0);
                final MotionEvent upEvent = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0);
                View view = mSongList.getChildAt(mSongList.getChildCount() - 1 - (mSongList.getLastVisiblePosition() - index)).findViewById(R.id.id_song_item_container);
                view.onTouchEvent(downEvent);
                view.onTouchEvent(upEvent);
                downEvent.recycle();
                upEvent.recycle();*/
                    mAdapter.recordLastClickPosition(mCurrentPosition - 1);
                    changeNotificationState();
                    //播放歌曲
                    toPlaySong();
                } else {
                    Toast.makeText(GameMainActivity.this, "当前已经是第一首歌", Toast.LENGTH_SHORT).show();
                }

            }
        }
        //如果是循环模式
        else if (mPlayMode == LOOP_MODE) {
            if (mMusicReady && mCurrentPosition != -1) {
                if (mCurrentPosition - 1 >= 1) {
                    mCurrentPosition--;
                    mAdapter.recordLastClickPosition(mCurrentPosition - 1);
                    changeNotificationState();
                    toPlaySong();
                } else {
                    mCurrentPosition = mSongsInfo.size();
                    mAdapter.recordLastClickPosition(mCurrentPosition - 1);
                    changeNotificationState();
                    toPlaySong();
                }
            }
        }
        //如果是随机模式
        else if (mPlayMode == RANDOM_MODE) {
            if (mMusicReady && mCurrentPosition != -1) {
                mCurrentPosition = new Random().nextInt(mSongsInfo.size()) + 1;
                mAdapter.recordLastClickPosition(mCurrentPosition - 1);
                changeNotificationState();
                toPlaySong();
            }
        }
    }

    private void playNextSong() {
        if (mPlayMode == LIST_MODE) {
            if (mMusicReady && mCurrentPosition != -1) {
                //mSongsInfo.size() + 1是由于添加了head
                if (mCurrentPosition + 1 < mSongsInfo.size() + 1) {
                    mCurrentPosition++;
                    mAdapter.recordLastClickPosition(mCurrentPosition - 1);
                    changeNotificationState();
                    toPlaySong();
                } else {
                    Toast.makeText(GameMainActivity.this, "当前已经是最后一首歌", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (mPlayMode == LOOP_MODE) {
            if (mMusicReady && mCurrentPosition != -1) {
                if (mCurrentPosition + 1 < mSongsInfo.size() + 1) {
                    mCurrentPosition++;
                    mAdapter.recordLastClickPosition(mCurrentPosition - 1);
                    changeNotificationState();
                    toPlaySong();
                } else {
                    mCurrentPosition = 1;
                    mAdapter.recordLastClickPosition(mCurrentPosition - 1);
                    changeNotificationState();
                    toPlaySong();
                }
            }
        } else if (mPlayMode == RANDOM_MODE) {
            if (mMusicReady && mCurrentPosition != -1) {
                mCurrentPosition = new Random().nextInt(mSongsInfo.size()) + 1;
                mAdapter.recordLastClickPosition(mCurrentPosition - 1);
                changeNotificationState();
                toPlaySong();
            }
        }
    }


    private void pauseSongIfCanBe() {
        if (mMusicReady && mSongService.isPlayerInitFinish()) {
            if (mSongService.isPlaying()) {
                mSongService.pause();
                mPlay.setVisibility(View.VISIBLE);
                mPause.setVisibility(View.GONE);
                //如果应用没有运行在前台,则发送通知,以便在通知栏控制音乐的播放
                if (!ActivityStateHelper.isAppOnForeground(GameMainActivity.this)) {
                    RemoteViews contentView = getRemoteViews();
                    contentView.setImageViewResource(R.id.id_statusBar_play, R.mipmap.play_gray_icon);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mNotificationManager.notify(0, mBuilder.build());
                    } else {
                        mNotificationManager.notify(0, mNotification);
                    }
                }
            }
        }
    }

    private void playSongIfCanBe() {
        if (mMusicReady && mSongService.isPlayerInitFinish() && !mSongService.isPlaying() && mCurrentPosition != -1) {
            mSongService.start();
            mPlay.setVisibility(View.GONE);
            mPause.setVisibility(View.VISIBLE);
            mTimeChangeEs.execute(GameMainActivity.this);
            //如果应用没有运行在前台,则发送通知,以便在通知栏控制音乐的播放
            if (!ActivityStateHelper.isAppOnForeground(GameMainActivity.this)) {
                RemoteViews contentView = getRemoteViews();
                contentView.setImageViewResource(R.id.id_statusBar_play, R.mipmap.pause_gray_icon_small);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mNotificationManager.notify(0, mBuilder.build());
                } else {
                    mNotificationManager.notify(0, mNotification);
                }
            }
        } else {
            Toast.makeText(GameMainActivity.this, "请选择要播放的歌曲", Toast.LENGTH_SHORT).show();
        }
    }

    private void toPlaySong() {
        if (mSongService.isPlaying()) {
            mSongService.pause();
        }
        //将除了head的当前可见的item的歌曲名字先置为黑色
        if (mSongList.getLastVisiblePosition() + 1 != mSongList.getChildCount()) {
            for (int i = 0; i < mSongList.getChildCount(); i++) {
                View view = mSongList.getChildAt(i);
                if (view != null) {
                    ((TextView) (view.findViewById(R.id.id_song_name))).setTextColor(Color.BLACK);
                }
            }
        } else {
            for (int i = 1; i < mSongList.getChildCount(); i++) {
                View view = mSongList.getChildAt(i);
                if (view != null) {
                    ((TextView) (view.findViewById(R.id.id_song_name))).setTextColor(Color.BLACK);
                }
            }
        }
        //如果将要播放的歌曲在当前可见,则将歌曲名字设置成红色
        int index = mSongList.getLastVisiblePosition() - mCurrentPosition;
        if (index < mSongList.getChildCount()) {
            View view = mSongList.getChildAt(mSongList.getChildCount() - 1 - index);
            if (view != null) {
                ((TextView) (view.findViewById(R.id.id_song_name))).setTextColor(Color.RED);
            }
        }
        //获得要播放的歌曲信息
        SongBean songBean = mSongsInfo.get(mCurrentPosition - 1);
        //完成歌曲的后台准备并播放
        mSongService.reset();
        mSongService.setDataSource(songBean.getPath());
        mSongService.prepare();
        mSongService.start();
        mPlay.setVisibility(View.GONE);
        mPause.setVisibility(View.VISIBLE);
        //重置seekBar的最大值
        int duration = mSongService.getDuration();
        mSongSeekBar.setMax(duration);
        mDuration.setText(toTime(duration));
        mTimeChangeEs.execute(GameMainActivity.this);
    }

    private void changeNotificationState() {
        if (!ActivityStateHelper.isAppOnForeground(this)) {
            RemoteViews contentView = getRemoteViews();
            SongBean songBean = mSongsInfo.get(mCurrentPosition - 1);
            contentView.setTextViewText(R.id.id_statusBar_song_name, songBean.getName());
            contentView.setTextViewText(R.id.id_statusBar_singer, songBean.getSinger());
            contentView.setImageViewResource(R.id.id_statusBar_play, R.mipmap.pause_gray_icon_small);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mNotificationManager.notify(0, mBuilder.build());
            } else {
                mNotificationManager.notify(0, mNotification);
            }
        }
    }


    @NonNull
    private RemoteViews getRemoteViews() {
        //如果Android3.0及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mBuilder = new Notification.Builder(GameMainActivity.this);
            mBuilder.setTicker(null);
            mBuilder.setContentText("");
            mBuilder.setSmallIcon(R.mipmap.play_gray_icon);
            //通知栏布局
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.remote_view);
            //设置通知栏显示布局
            mBuilder.setContent(contentView);
            //设置通知栏播放按钮的响应事件
            Intent playIntent = new Intent(ACTION_PLAY);
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(GameMainActivity.this, 0, playIntent, PendingIntent.FLAG_ONE_SHOT);
            contentView.setOnClickPendingIntent(R.id.id_statusBar_play, playPendingIntent);
            //设置通知栏下一首按钮的响应事件
            Intent nextIntent = new Intent(ACTION_NEXT);
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(GameMainActivity.this, 0, nextIntent, PendingIntent.FLAG_ONE_SHOT);
            contentView.setOnClickPendingIntent(R.id.id_statusBar_next, nextPendingIntent);
            //设置通知栏上一首按钮的响应事件
            Intent previousIntent = new Intent(ACTION_PREVIOUS);
            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(GameMainActivity.this, 0, previousIntent, PendingIntent.FLAG_ONE_SHOT);
            contentView.setOnClickPendingIntent(R.id.id_statusBar_previous, previousPendingIntent);
            //设置通知栏点击返回界面事件
            Intent gameIntent = new Intent();
            //gameIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            gameIntent.setComponent(new ComponentName(this.getPackageName(), this.getPackageName() + "." + this.getLocalClassName()));
            //设置启动方式,防止点击通知栏返回时或者点击应用图标进入游戏时再多开启一个目标Activity
            //在manifest activity中配置android:launchMode="singleTask"
            //gameIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mBuilder.setContentIntent(PendingIntent.getActivity(GameMainActivity.this, 0, gameIntent, PendingIntent.FLAG_ONE_SHOT));
            return contentView;
        }
        //如果Android3.0以下
        else {
            mNotification = new Notification(R.mipmap.play_gray_icon, "", SystemClock.currentThreadTimeMillis());
            mNotification.flags = Notification.FLAG_AUTO_CANCEL;
            //通知栏布局
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.remote_view);
            //设置通知栏显示布局
            mNotification.contentView = contentView;
            //设置通知栏播放按钮的响应事件
            Intent playIntent = new Intent(ACTION_PLAY);
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(GameMainActivity.this, 0, playIntent, PendingIntent.FLAG_ONE_SHOT);
            contentView.setOnClickPendingIntent(R.id.id_statusBar_play, playPendingIntent);
            //设置通知栏下一首按钮的响应事件
            Intent nextIntent = new Intent(ACTION_NEXT);
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(GameMainActivity.this, 0, nextIntent, PendingIntent.FLAG_ONE_SHOT);
            contentView.setOnClickPendingIntent(R.id.id_statusBar_next, nextPendingIntent);
            //设置通知栏上一首按钮的响应事件
            Intent previousIntent = new Intent(ACTION_PREVIOUS);
            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(GameMainActivity.this, 0, previousIntent, PendingIntent.FLAG_ONE_SHOT);
            contentView.setOnClickPendingIntent(R.id.id_statusBar_previous, previousPendingIntent);
            //设置通知栏点击返回界面事件
            Intent gameIntent = new Intent(GameMainActivity.this, GameMainActivity.class);
            gameIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            //gameIntent.setComponent(new ComponentName(this.getPackageName(), this.getPackageName() + "." + this.getLocalClassName()));
            //gameIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mNotification.contentIntent = PendingIntent.getActivity(GameMainActivity.this, 0, gameIntent, PendingIntent.FLAG_ONE_SHOT);
            return contentView;
        }
    }

    private String toTime(int time) {
        int minute = time / 1000 / 60;
        int second = time / 1000 % 60;
        String mm, ss;
        if (minute < 10) {
            mm = "0" + minute;
        } else {
            mm = minute + "";
        }
        if (second < 10) {
            ss = "0" + second;
        } else {
            ss = second + "";
        }
        return mm + ":" + ss;
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


    private void changeToSettingActionBar() {
        ActionBarHelper.showCustomActionBar(mActionBar, customView_setting);
        TextView music = (TextView) customView_setting.findViewById(R.id.id_actionbar_music);
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlideView.toggle();
            }
        });
    }

    private void changeToPhotoActionBar() {
        ActionBarHelper.showCustomActionBar(mActionBar, customView_photo);
        TextView music = (TextView) customView_photo.findViewById(R.id.id_actionbar_music);
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlideView.toggle();
            }
        });
    }

    private void changeToGameActionBar() {
        ActionBarHelper.showCustomActionBar(mActionBar, customView_main);
        TextView back = (TextView) customView_main.findViewById(R.id.id_actionbar_back);
        TextView music = (TextView) customView_main.findViewById(R.id.id_actionbar_music);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialogPopupWindow alertDialog = new AlertDialogPopupWindow(GameMainActivity.this);
                alertDialog.setTitle("提示");
                alertDialog.setContent("即将退出游戏");
                alertDialog.addPicture(R.mipmap.miss);
                alertDialog.setPositiveButton("确定", new AlertDialogPopupWindow.OnClickListener() {
                    @Override
                    public void onClick() {
                        alertDialog.dismiss();
                        GameMainActivity.this.finish();
                    }
                });
                alertDialog.setNegativeButton("取消", new AlertDialogPopupWindow.OnClickListener() {
                    @Override
                    public void onClick() {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlideView.toggle();
            }
        });
    }


    private void handleResult(Fragment fragment, int requestCode, int resultCode,
                              Intent data) {
        fragment.onActivityResult(requestCode & 0xffff, resultCode, data);
        List<Fragment> frags = fragment.getChildFragmentManager().getFragments();
        if (frags != null) {
            for (Fragment f : frags) {
                if (f != null)
                    handleResult(f, requestCode, resultCode, data);
            }
        }
    }
}
