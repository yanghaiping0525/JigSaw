package com.yang.jigsaw.http;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.yang.jigsaw.bean.FileInfo;
import com.yang.jigsaw.bean.ThreadInfo;
import com.yang.jigsaw.dao.ThreadDAO;
import com.yang.jigsaw.dao.ThreadDAOImpl;
import com.yang.jigsaw.service.DownLoadService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by YangHaiPing on 2016/3/6.
 */
public class DownLoadTask {
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mDao;
    //文件下载路径
    private String fileDir;
    public boolean isPause = false;
    //线程个数
    public static final int THREAD_COUNT = 3;
    //执行下载任务的线程池
    private Executor threadPool = Executors.newFixedThreadPool(9);

    public DownLoadTask(Context context, FileInfo fileInfo) {
        this.mContext = context;
        this.mFileInfo = fileInfo;
        this.mDao = new ThreadDAOImpl(context);
        fileDir = context.getFilesDir().getAbsolutePath();
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void downLoad() {
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
        //如果没有待下载的任务
        if (threadInfos.size() == 0) {
            //将下载文件分成若干份,由若干个线程同时下载
            int block = mFileInfo.getLength() / THREAD_COUNT;
            for (int i = 0; i < THREAD_COUNT; i++) {
                int start = block * i;
                int end = (i + 1) * block - 1;
                if (i == THREAD_COUNT - 1) {
                    end = mFileInfo.getLength();
                }
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), start, end, 0);
                Runnable runnable = new DownLoadRunnable(threadInfo, mFileInfo);
                threadPool.execute(runnable);
            }
        }
        //如果数据库有线程信息,说明有任务还没有完成,继续完成该线程的下载任务
        else {
            for (int i = 0; i < threadInfos.size(); i++) {
                ThreadInfo threadInfo = threadInfos.get(i);
                Runnable runnable = new DownLoadRunnable(threadInfo, mFileInfo);
                threadPool.execute(runnable);
            }
        }

    }

    class DownLoadRunnable implements Runnable {
        private ThreadInfo mThreadInfo;
        private FileInfo mFileInfo;
        int mFinished = 0;

        public DownLoadRunnable(ThreadInfo mThreadInfo, FileInfo fileInfo) {
            this.mThreadInfo = mThreadInfo;
            this.mFileInfo = fileInfo;
        }

        @Override
        public void run() {
            //向数据库插入线程信息
            if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDao.insertThread(mThreadInfo);
            }
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            BufferedInputStream bis = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                //拿到下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                //设置下载位置
                connection.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                //设置文件写入位置
                File file = new File(fileDir, mFileInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                //累计已完成的大小
                mFinished += mThreadInfo.getFinished();
                //请求已经被完成，并且web程序(客户端程序浏览器程序)已经重置了文档视图目录（content），这个目录很容易允许使用者用另一个动作发送请求
                if (connection.getResponseCode() == 206) {
                    //读取数据
                    bis = new BufferedInputStream(connection.getInputStream());
                    byte[] buff = new byte[1024 * 4];
                    int len;
//                    long time = System.currentTimeMillis();
                    while ((len = bis.read(buff)) != -1) {
                        //写入文件
                        raf.write(buff, 0, len);
                        //更新完成进度
                        mFinished += len;
//                        if (System.currentTimeMillis() - time >= 100) {
//                            time = System.currentTimeMillis();
//                            intent.putExtra("finished", ((int) (mFinished * (100 / (float) mFileInfo.getLength()))));
//                            intent.putExtra("threadId", mThreadInfo.getId());
//                            mContext.sendBroadcast(intent);
//                        }
                        //在下载暂停时，把进度保存到数据库
                        if (isPause) {
                            mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            return;
                        }
                    }
                    //该线程完成下载任务后发生广播通知
                    Intent intent = new Intent(DownLoadService.ACTION_UPDATE);
                    intent.putExtra("finish", "1");
                    intent.putExtra("threadId", mThreadInfo.getId());
                    intent.putExtra("name", mFileInfo.getName());
                    mContext.sendBroadcast(intent);
                    //任务完成,删除该线程信息
                    mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
                }
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
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

}
