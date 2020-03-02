package com.moosphon.downloader.listener;

import android.graphics.Bitmap;

/**
 * The progress callback of image downloading.
 */
public interface DownloadProgressListener {

    void onProgressUpdate(int current, int total);
    void onComplete(String url, Bitmap bitmap);
    void onFailed(Exception e);
}
