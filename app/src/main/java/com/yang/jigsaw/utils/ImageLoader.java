package com.yang.jigsaw.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by YangHaiPing on 2016/1/30.
 * 该类需要管理缓存，得使用单例模式
 */
public class ImageLoader {
    // 图片缓存 android.util.LruCache JDK要求API 12 这里导入v4兼容包的LruCache
    // 里面不能使用软引用,当对象被回收后调用sizeOf方法会造成空指针异常
    private LruCache<String, Bitmap> mLruCache;
    // 线程池
    private ExecutorService mThreadPoolEs;
    private static final int DEFAULT_THREAD_COUNT = 6;
    // 队列调度方式(默认先进先出)
    private QueueType mQueueType = QueueType.FIFO;
    // 任务队列
    private LinkedList<Runnable> mTaskQueue;
    private Handler mThreadPoolHandler;
    private Handler mUIHandler;
    //信号量控制线程的执行
    private Semaphore mPoolThreadHandlerSem = new Semaphore(0);
    private Semaphore mThreadTaskSem;
    private boolean isOnce = false;
    private static final int POOL_THREAD_MESSAGE = 0x100;
    //记录正在加载的图片路径或者url防止图片重复加载
    private List<String> mDecodingPaths = new ArrayList<>();

    //枚举选择调度方式
    public enum QueueType {
        FIFO, LIFO
    }

    private ImageLoader(int threadCount, QueueType queueType) {
        init(threadCount, queueType);
    }

    private static ImageLoader mInstance;

    public static ImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEFAULT_THREAD_COUNT, QueueType.FIFO);
                }
            }
        }
        return mInstance;
    }

    public static ImageLoader getInstance(int ThreadCount, QueueType queueType) {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(ThreadCount, queueType);
                }
            }
        }
        return mInstance;
    }

    private void init(int threadCount, QueueType queueType) {

        //开启线程接收消息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mThreadPoolHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == POOL_THREAD_MESSAGE) {
                            //阻塞直到任务添加到已经任务链中,避免getTask()抛出空指针
                            try {
                                mThreadTaskSem.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Runnable task = getTask();
                            if (task != null) {
                                mThreadPoolEs.execute(task);
                            }
                        }
                    }
                };
                if (!isOnce) {
                    mPoolThreadHandlerSem.release();
                    isOnce = true;
                }
                Looper.loop();
            }
        }.start();

        mUIHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ImageHolder imageHolder = (ImageHolder) msg.obj;
                Bitmap bitmap = imageHolder.bitmap;
                ImageView imageView = imageHolder.imageView;
                String path = imageHolder.path;
                if (imageView.getTag().toString().equals(path)) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        };

        // 获取应用的最大使用内存
        long maxMemory = Runtime.getRuntime().maxMemory();
        //设置缓存大小为最大内存的八分之一
        int cacheMemory = (int) (maxMemory / 8);
        //初始化缓存
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            // 测量每个Bitmap的值,默认cache大小是测量的item的数量，重写sizeof计算不同item的大小
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 每一行所占据的字节数乘以Bitmap的高度
                return bitmap.getRowBytes() * bitmap.getHeight();
//                return bitmap.getByteCount();//API12
            }
        };
        //获得当前系统的CPU数量
        int cupNum = Runtime.getRuntime().availableProcessors();
        //初始化线程池对象
        mThreadPoolEs = Executors.newFixedThreadPool(threadCount * cupNum);
        //初始化任务链表
        mTaskQueue = new LinkedList<>();
        mQueueType = queueType;
        //初始化线程任务信号量
        mThreadTaskSem = new Semaphore(threadCount);
    }

    //添加任务并发送消息请求执行
    private void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        //mThreadPoolHandler未必已经完成初始化，可能造成空指针异常，需要引入同步信号量控制代码执行时序
        //如果mThreadPoolHandler还没完成初始化则先进行阻塞,等待mThreadPoolHandler完成初始化后释放信号解除阻塞
        if (mThreadPoolHandler == null) {
            synchronized (ImageHolder.class) {
                if (mThreadPoolHandler == null) {
                    try {
                        //阻塞该线程
                        mPoolThreadHandlerSem.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        mThreadPoolHandler.sendEmptyMessage(POOL_THREAD_MESSAGE);
    }

    //根据调度方式选择该执行的任务
    private Runnable getTask() {
        if (mQueueType == QueueType.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mQueueType == QueueType.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    public void loadImageFromUrl(final String url, final ImageView imageView) {
        if (mDecodingPaths.contains(url)) {
            return;
        }
        imageView.setTag(url);
        //先尝试从缓存中获得bitmap
        Bitmap bitmap = mLruCache.get(url);
        //如果缓存中有该bitmap对象,则发送通知更新UI即可
        if (bitmap != null) {
            Message message = Message.obtain();
            ImageHolder imageHolder = new ImageHolder();
            imageHolder.bitmap = bitmap;
            imageHolder.path = url;
            imageHolder.imageView = imageView;
            message.obj = imageHolder;
            mUIHandler.sendMessage(message);
        }
        //如果缓存中没有该bitmap对象,则将任务添加到任务队列,等待线程完成下载任务
        else {
            mDecodingPaths.add(url);
            addTask(new Runnable() {
                @Override
                public void run() {
                    //获得imageView的大小
                    ImageSize imageSize = getImageViewSize(imageView);
                    //通过网络获得bitmap对象
                    Bitmap bm = decodeBitmapFromUrl(url, imageSize.width, imageSize.height);
                    //将bitmap添加到缓存中
                    addBitmapToLruCache(url, bm);
                    Message message = Message.obtain();
                    ImageHolder imageHolder = new ImageHolder(bm, imageView, url);
                    message.obj = imageHolder;
                    mUIHandler.sendMessage(message);
                    mDecodingPaths.remove(url);
                    mThreadTaskSem.release();
                }
            });
        }

    }


    public void loadImageFromPath(final String path, final ImageView imageView) {
        if (mDecodingPaths.contains(path)) {
            return;
        }
        imageView.setTag(path);
        //先尝试从缓存中获得bitmap
        Bitmap bitmap = mLruCache.get(path);
        //如果缓存中有该bitmap对象,则发送通知更新UI即可
        if (bitmap != null) {
            Message message = Message.obtain();
            ImageHolder imageHolder = new ImageHolder();
            imageHolder.bitmap = bitmap;
            imageHolder.path = path;
            imageHolder.imageView = imageView;
            message.obj = imageHolder;
            mUIHandler.sendMessage(message);
        }
        //如果缓存中没有该bitmap对象,则将任务添加到任务队列,等待线程完成加载任务
        else {
            mDecodingPaths.add(path);
            addTask(new Runnable() {
                @Override
                public void run() {
                    //获得imageView的大小
                    ImageSize imageSize = getImageViewSize(imageView);
                    //获得压缩后的bitmap
                    Bitmap bm = decodeBitmapFromPath(path, imageSize.width, imageSize.height);
                    //将压缩后的bitmap添加到缓存中
                    addBitmapToLruCache(path, bm);
                    //发送消息更新UI
                    Message message = Message.obtain();
                    ImageHolder imageHolder = new ImageHolder(bm, imageView, path);
                    message.obj = imageHolder;
                    mUIHandler.sendMessage(message);
                    mDecodingPaths.remove(path);
                    mThreadTaskSem.release();
                }
            });
        }
    }

    private Bitmap decodeBitmapFromUrl(String url, int width, int height) {
        URL imageUrl;
        Bitmap bitmap;
        HttpURLConnection connection = null;
        BufferedInputStream bif = null;
        try {
            imageUrl = new URL(url);
            connection = (HttpURLConnection) imageUrl.openConnection();
            bif = new BufferedInputStream(connection.getInputStream());
//            BitmapFactory.Options options = new BitmapFactory.Options();
            // 先计算图片的压缩比例，暂且不把图片加载到内存中
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeStream(bif, null, options);
//            // 计算压缩比例
//            options.inSampleSize = calculateInSampleSize(options, width, height);
//            options.inJustDecodeBounds = false;
//            bitmap = BitmapFactory.decodeStream(bif, null, options);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeStream(bif, null, options);
//            bitmap = BitmapFactory.decodeStream(bif);
            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (bif != null) {
                try {
                    bif.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Bitmap decodeBitmapFromPath(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 先计算图片的压缩比例，暂且不把图片加载到内存中
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // 计算压缩比例
        options.inSampleSize = calculateInSampleSize(options, width, height);
        // 可将图片加载到内存
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int width, int height) {
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int inSampleSize = 1;
        if (outWidth > width || outHeight > height) {
            int widthRatio = Math.round(outWidth * 1.0f / width);
            int heightRatio = Math.round(outHeight * 1.0f / height);
            inSampleSize = Math.max(widthRatio, heightRatio) + 1;
        }
        return inSampleSize;
    }

    private void addBitmapToLruCache(String path, Bitmap bitmap) {
        if (mLruCache.get(path) == null) {
            if (bitmap != null) {
                mLruCache.put(path, bitmap);
            }
        }
    }


    private ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        int width = imageView.getWidth();
        // imageView在容器中声明的宽度(wrap_content = -1,match_parent = -2)
        if (width <= 0) {
            width = params.width;
        }
        if (width <= 0) {
            // width = imageView.getMaxWidth();//add in api level 16
            width = getImageViewFieldValue(imageView, "mMaxWidth");
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;
        }
        int height = imageView.getHeight();
        if (height <= 0) {
            height = getImageViewFieldValue(imageView, "mMaxHeight");
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }
        imageSize.width = width;
        imageSize.height = height;
        return imageSize;
    }

    private int getImageViewFieldValue(ImageView imageView, String fileName) {
        int value = 0;
        Field field = null;
        try {
            field = ImageView.class.getDeclaredField(fileName);
            field.setAccessible(true);
            int fieldValue = field.getInt(imageView);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        return value;
    }


    private class ImageSize {
        int width;
        int height;
    }

    private class ImageHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;

        public ImageHolder() {

        }

        public ImageHolder(Bitmap bitmap, ImageView imageView, String path) {
            this.bitmap = bitmap;
            this.imageView = imageView;
            this.path = path;
        }
    }


}
