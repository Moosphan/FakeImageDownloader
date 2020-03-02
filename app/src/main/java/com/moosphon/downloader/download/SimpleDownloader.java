package com.moosphon.downloader.download;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.moosphon.downloader.listener.DownloadProgressListener;
import com.moosphon.downloader.listener.TasksDownloadStateListener;
import com.moosphon.downloader.task.DownloadImageTask;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A brief image downloader.
 * @author Moosphon
 */
public class SimpleDownloader {

    public static final int MSG_PROGRESS = 12;
    public static final int MSG_SUCCESS = 13;
    public static final int MSG_FAILED = 14;

    // Thread pool executor
    private ExecutorService mThreadPool;
    // Tasks to run
    private LinkedList<DownloadImageTask> mTasks;
    // UI update handler
    private Handler mUiHandler;
    // Callback to listener state of tasks
    private TasksDownloadStateListener mStateListener;

    // singleton
    public static SimpleDownloader getInstance() {
        return LazyHolder.INSTANCE;
    }

    public SimpleDownloader init(List<String> urls, int threadCount, TasksDownloadStateListener listener) {
        mStateListener = listener;
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTasks = new LinkedList<>();
        for (String resourceUrl : urls) {
            addTask(resourceUrl);
        }
        return this;
    }

    private void addTask(String resourceUrl) {
        mTasks.add(new DownloadImageTask(resourceUrl, new DownloadProgressListener() {
            @Override
            public void onProgressUpdate(int current, int total) {
                float progress = current / (total * 1f);
                Message message = Message.obtain();
                message.obj = progress;
                message.what = MSG_PROGRESS;
                mUiHandler.sendMessage(message);
            }

            @Override
            public void onComplete(String url, Bitmap bitmap) {
                Message message = Message.obtain();
                message.obj = bitmap;
                message.what = MSG_SUCCESS;
                mUiHandler.sendMessage(message);
                if (checkIfAllComplete()) {
                    if (mStateListener != null) {
                        mStateListener.allComplete();
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Message message = Message.obtain();
                message.obj = e.getMessage();
                message.what = MSG_FAILED;
                mUiHandler.sendMessage(message);
            }
        }));
    }

    /**
     * bind handler to display images on views.
     * @param handler {@link #mUiHandler}
     */
    public SimpleDownloader bindMessager(Handler handler) {
        this.mUiHandler = handler;
        return this;
    }

    public void startWorking() {
        if (mThreadPool == null) {
            throw new IllegalArgumentException("Please initialize #SimpleDownloader first!");
        }
        for (DownloadImageTask task : mTasks) {
            mThreadPool.execute(task);
        }
    }

    private synchronized boolean checkIfAllComplete() {
        boolean finished = true;
        for (DownloadImageTask task : mTasks) {
            if (task.isDownloading()) {
                finished = false;
            }
        }
        return finished;
    }




    private SimpleDownloader() {}

    private static class LazyHolder {
        static final SimpleDownloader INSTANCE = new SimpleDownloader();
    }
}
