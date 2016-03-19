package com.yang.jigsaw.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yang.jigsaw.bean.FileInfo;
import com.yang.jigsaw.http.DownLoadTask;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by YangHaiPing on 2016/3/6.
 */
public class DownLoadService extends Service {
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final int MSG_INIT = 0;
    private boolean isOnce = false;
    //自定义类完成下载任务
    private DownLoadTask mDownLoadTask;
    //记录下载任务中的多个下载线程(参数一,任务文件的名称,参数二,线程的名称,参数三,已完成分支任务的线程数量)
    private Map<String, Map<Integer, Integer>> mTaskRunningMap = new HashMap<>();
    //记录保存多个下载任务
    private Map<String, DownLoadTask> mTaskMap = new HashMap<>();
    //文件下载目录
    private String mDir;
    //接收下载完成的广播
    private BroadcastReceiver mDownLoadStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_UPDATE)) {
                synchronized (DownLoadService.class) {
                    int mThreadId = intent.getIntExtra("threadID", 0);
                    String name = intent.getStringExtra("name");
                    //遍历Map集合根据文件名称来判断是否是新的任务
                    boolean isNewTask = true;
                    Iterator<Map.Entry<String, Map<Integer, Integer>>> it = mTaskRunningMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, Map<Integer, Integer>> entry = it.next();
                        if (entry.getKey().equals(name)) {
                            isNewTask = false;
                            break;
                        }
                    }
                    //如果是新的任务则加入到Map中
                    if (isNewTask) {
                        Map<Integer, Integer> map = new HashMap<>();
                        map.put(mThreadId, 1);
                        mTaskRunningMap.put(name, map);
                    }
                    //更新任务中完成分支任务的线程的数量
                    else {
                        Map<Integer, Integer> task = mTaskRunningMap.get(name);
                        int finish = task.get(mThreadId) + 1;
                        task.put(mThreadId, finish);
                        //如果该任务中全部线程均完成分支任务,将文件路径添加到配置文件中,并广播通知我的图片Activity更新数据
                        if (finish == DownLoadTask.THREAD_COUNT) {
                            mTaskRunningMap.remove(name);
                            String fileName = intent.getStringExtra("name");
                            String filePath = mDir + "/" + fileName;
                            MediaScannerConnection.scanFile(DownLoadService.this, new String[]{filePath}, null, null);
                            SharedPreferences sharedPreferences = getSharedPreferences("imagePaths", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            Set<String> filePaths = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                filePaths = sharedPreferences.getStringSet("imagePaths", new HashSet<String>());
                            }

                            if (!filePaths.contains(filePath)) {
                                filePaths.add(filePath);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                editor.clear();
                                editor.commit();
                                editor.putStringSet("imagePaths", filePaths);
                                editor.commit();
                            }
                            Intent finishIntent = new Intent("downLoadFinish");
                            sendBroadcast(finishIntent);
                        }
                    }
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isOnce) {
            mDir = DownLoadService.this.getFilesDir().getAbsolutePath();
            //注册广播用于接收下载线程完成任务后发送的广播
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_UPDATE);
            registerReceiver(mDownLoadStateReceiver, intentFilter);
            isOnce = true;
        }

        //有任务请求开始
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            new InitThread(fileInfo).start();
        }
        //有任务请求结束(这里是断点下载的拓展,由于只是下载图片本例中没有设置暂停下载按钮)
        else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            DownLoadTask downLoadTask = mTaskMap.get(fileInfo.getUrl());
            if (downLoadTask != null) {
                downLoadTask.isPause = true;
            }
            mTaskMap.remove(fileInfo.getUrl());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    //启动下载任务
                    mDownLoadTask = new DownLoadTask(DownLoadService.this, fileInfo);
                    //记录下载中的任务,用于处理断点下载
                    mTaskMap.put(fileInfo.getUrl(), mDownLoadTask);
                    mDownLoadTask.downLoad();
                    break;
            }
        }
    };

    //该类作用：获得网络资源信息,记录资源的大小
    class InitThread extends Thread {
        private FileInfo mFileInfo;

        public InitThread(FileInfo fileInfo) {
            this.mFileInfo = fileInfo;
        }

        public void run() {

            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                //设置超时时间3秒
                connection.setConnectTimeout(3000);
                //下载文件用get
                connection.setRequestMethod("GET");
                int length = -1;
                if (connection.getResponseCode() == 200) {
                    length = connection.getContentLength();
                }
                if (length <= 0) {
                    return;
                }

                File file = new File(mDir, mFileInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
