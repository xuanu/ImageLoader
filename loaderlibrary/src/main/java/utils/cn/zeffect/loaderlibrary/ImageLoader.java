package utils.cn.zeffect.loaderlibrary;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 图片加载类
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/04/12
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 */

public class ImageLoader {
    public static ImageLoader instance;

    public static ImageLoader getInstance() {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader();
                }
            }
        }
        return instance;
    }

    /***
     * 内存级存
     */
    private LruCache<String, Bitmap> mLruCache;
    /***
     * 线程池
     */
    private ExecutorService mExecutors;
    /***
     * 默认只有一个线程
     */
    private static final int DEFAULT_THEAD_COUNT = 1;
    /***
     * 任务队列，每次任务过来，先增加到任务队列
     */
    private LinkedList<Task> mTaskQueue;
    /***
     * 主线程Handler，用来更新界面
     */
    private Handler mUiHandler;
    /***
     * 后台线程
     */
    private Handler mBacgroudHandler;
    /***
     * 信号量，用来控制一次加载量
     */
    private Semaphore mSemaphore;


    private ImageLoader() {
        initCache();
    }

    private void initCache() {
        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cache = maxCache / 8;
        mLruCache = new LruCache<String, Bitmap>(cache) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        mExecutors = Executors.newFixedThreadPool(DEFAULT_THEAD_COUNT);
        mTaskQueue = new LinkedList<>();
        mSemaphore = new Semaphore(DEFAULT_THEAD_COUNT);
        //
        mUiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ImageViewBean tempBean = (ImageViewBean) msg.obj;
                tempBean.getImageView().setImageBitmap(tempBean.getBitmap());
            }
        };
        mBacgroudHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mExecutors.execute(mTaskQueue.removeLast().getRunnable());
                try {
                    mSemaphore.acquire();
                } catch (InterruptedException pE) {
                    pE.printStackTrace();
                }
            }
        };
    }


    /***
     * 加载图片
     * @param path  图片路径
     * @param pImageView ImageView
     */
    public void load(String path, ImageView pImageView) {
        if (TextUtils.isEmpty(path) || pImageView == null) {
            return;
        }
        pImageView.setTag(path);
        Bitmap cacheBitmap = getLruCache(path);
        if (cacheBitmap != null) {
            //发消息给主进程，更新图片
            updaterImageView(path, pImageView, cacheBitmap);
        } else {
            addTask(path, pImageView);
        }

    }

    /***
     * 添加一个任务
     * @param path          路径
     * @param pImageView 控件
     */
    private synchronized void addTask(String path, ImageView pImageView) {
        for (int i = 0; i < mTaskQueue.size(); i++) {
            Task tempTask = mTaskQueue.get(i);
            if (tempTask.getPath().equals(path) && pImageView == tempTask.getImageView()) {
                return;
            }
        }
        //
        mTaskQueue.add(buildTask(path, pImageView));
        mBacgroudHandler.sendEmptyMessage(0x01);
    }

    /****
     * 构建一个任务
     * @param path   路径
     * @param pImageView 控件
     * @return 任务
     */
    private Task buildTask(final String path, final ImageView pImageView) {
        return new Task().setImageView(pImageView).setPath(path).setRunnable(new Runnable() {
            @Override
            public void run() {
                //执行加载图片的具体操作
                Bitmap cacheBitmap = null;
                cacheBitmap = ImageUtils.getBitmap(new File(getCacheDirs(), path));
                addLruCache(path, cacheBitmap);
                updaterImageView(path, pImageView, cacheBitmap);
                mSemaphore.release();
            }
        });
    }

    /***
     * 返回缓存目录
     * @return context==null返回SD卡根目录，否则返回qimon/student/cache/包名/
     */
    public static String getCacheDirs() {
        File tempFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "qimon" + File.separator + "student" + File.separator + "cache" + File.separator + "org.qimon.launcher6" + File.separator);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        return tempFile.getAbsolutePath();
    }

    /***
     * 更新控件
     * @param path 路径
     * @param pImageView 控件
     * @param pBitmap 得到的图
     */
    private void updaterImageView(String path, ImageView pImageView, Bitmap pBitmap) {
        if (TextUtils.isEmpty(path) || pImageView == null || pBitmap == null) {
            return;
        }
        Message tempMessage = Message.obtain();
        tempMessage.obj = new ImageViewBean().setBitmap(pBitmap).setPath(path).setImageView(pImageView);
        mUiHandler.sendMessage(tempMessage);
    }

    /**
     * 添加到内存缓存
     *
     * @param path        路径
     * @param cacheBitmap 图片
     * @return 真的保存return true;
     */
    private boolean addLruCache(String path, Bitmap cacheBitmap) {
        if (TextUtils.isEmpty(path) || cacheBitmap == null) {
            return false;
        }
        if (mLruCache.get(path) == null) {
            mLruCache.put(path, cacheBitmap);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 从缓存中取数据
     *
     * @param path 路径
     * @return Bitmap
     */
    private Bitmap getLruCache(String path) {
        return mLruCache.get(path);
    }

    /***
     * 带图片的控件
     */
    private static class ImageViewBean {
        private String path;
        private ImageView mImageView;
        private Bitmap mBitmap;

        public String getPath() {
            return path;
        }

        public ImageViewBean setPath(String pPath) {
            path = pPath;
            return this;
        }

        public ImageView getImageView() {
            return mImageView;
        }

        public ImageViewBean setImageView(ImageView pImageView) {
            mImageView = pImageView;
            return this;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public ImageViewBean setBitmap(Bitmap pBitmap) {
            mBitmap = pBitmap;
            return this;
        }
    }

    /***
     * 单个任务类
     */
    private static class Task {
        private String path;
        private ImageView mImageView;
        private Runnable mRunnable;

        public String getPath() {
            return path;
        }

        public Task setPath(String pPath) {
            path = pPath;
            return this;
        }

        public ImageView getImageView() {
            return mImageView;
        }

        public Task setImageView(ImageView pImageView) {
            mImageView = pImageView;
            return this;
        }

        public Runnable getRunnable() {
            return mRunnable;
        }

        public Task setRunnable(Runnable pRunnable) {
            mRunnable = pRunnable;
            return this;
        }
    }


}
