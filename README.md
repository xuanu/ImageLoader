# 图片缓存设计

> 基于文章的理解：[Android 框架练成 教你打造高效的图片加载框架](http://blog.csdn.net/lmj623565791/article/details/41874561)

- 本文未经过严格测试，使用时请注意

  > 1. 没有设置Tag时，是否会加载错乱
  > 2. 判断一个任务是否添加的方法是否正确

#### 1. 一个好的图片加载框架应该要处理的问题。

- [内存缓存LruCache](#内存缓存设计)  

- [快速滑动的加载策略](#快速滑动的加载策略)

  > 从头加载，还是从尾加载。（滑动到3000张时，从头加载，还是先加载当前的）

- 根据控件大小，缩放图片。（本文未做此功能）



##### 内存缓存设计<span id="内存缓存设计"></span>

> 内存缓存用LruCache，主要就是取数据，存数据。

```java
  /***
     * 内存级存
     */
    private LruCache<String, Bitmap> mLruCache;

    /***
     * 初始化缓存大小
     */
    private void initCache() {
        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cache = maxCache / 8;
        mLruCache = new LruCache<String, Bitmap>(cache) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
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
```

##### 快速滑动的加载策略<span id="快速滑动的加载策略"></span>

> 如果只是每次加载图片都直接用线程加载的话，是没有加载策略的。
>
> 所以这样做
>
> 1. 固定数量的线程池。
> 2. 每次先把任务加入一个队列，并不即时去加载图片
> 3. 用Semaphore控制加载数量。Semaphore要有许可acquire才能执行，否则就阻塞，只有当前release才能执行下一个。参考 [Java 并发专题 ： Semaphore 实现 互斥 与 连接池](http://blog.csdn.net/lmj623565791/article/details/26810813)
> 4. 这样就可以执行策略，比如一次加载3000张，前面没有加载的图片都还在队列里。当前Semaphore.release释放一个信号量时，就可以加载下一个任务的时候，从队列的尾端取任务。**本文并没有做这个策略，默认从队列尾部开始加载**



### Github代码[xuanu](https://github.com/xuanu)/[**ImageLoader**](https://github.com/xuanu/ImageLoader)

> 具体代码参见：以上链接 