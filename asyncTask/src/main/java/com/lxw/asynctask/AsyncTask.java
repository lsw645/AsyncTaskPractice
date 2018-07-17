package com.lxw.asynctask;

import java.util.ArrayDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AsyncTask<PARAMS, RESULT> {
    //获取 cpu数量
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    ;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final LinkedBlockingDeque<Runnable> LINKED_BLOCKING_DEQUE =
            new LinkedBlockingDeque<>(128);


    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };
    private static final Executor EXECUTOR_POOL;

    static {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS, LINKED_BLOCKING_DEQUE, sThreadFactory);
        executor.allowCoreThreadTimeOut(true);
        EXECUTOR_POOL = executor;
    }

    private static final Executor SERIAL_EXECUTOR = new Executor() {
        private ArrayDeque<Runnable> mDeque = new ArrayDeque<>();
        private Runnable isActive;

        @Override
        public synchronized void execute(final Runnable command) {
            mDeque.offer(new Runnable() {
                @Override
                public void run() {
                    try {
                        command.run();
                    } finally {
                        schduleNext();
                    }
                }
            });
            if (isActive == null) {
                schduleNext();
            }
        }

        public  synchronized  void schduleNext() {
            if ((isActive = mDeque.pop()) != null) {
                EXECUTOR_POOL.execute(isActive);
            }
        }

    };
    private final WorkRunnable mWorkRunnable;
    private final FutureTask mFutureTask;
    public AsyncTask() {
        mWorkRunnable = new WorkRunnable<PARAMS, RESULT>() {
            @Override
            public RESULT call() throws Exception {
                RESULT result = doInBackGround(mParams);
                {//使用handler即可回主线程
                    postResult(result);
                }
                return result;
            }
        };
        mFutureTask = new FutureTask<RESULT>(mWorkRunnable) {
            @Override
            protected void done() {
//                super.done();
                System.out.println("done");
            }
        };
    }

    public void execute(PARAMS... params) {
        mWorkRunnable.mParams = params;
        onPreExecute();
        SERIAL_EXECUTOR.execute(mFutureTask);
    }


    static abstract class WorkRunnable<PARAMS, RESULT> implements Callable<RESULT> {
        PARAMS[] mParams;
    }


    public abstract RESULT doInBackGround(PARAMS... params);

    public abstract void postResult(RESULT result);

    public abstract void onPreExecute();

}
