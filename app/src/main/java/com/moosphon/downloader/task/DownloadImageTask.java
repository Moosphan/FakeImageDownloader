package com.moosphon.downloader.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.moosphon.downloader.listener.DownloadProgressListener;
import com.moosphon.downloader.utils.IoUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Fetch image data from internet ruggedly.
 * @author Moosphon
 */
public final class DownloadImageTask implements Runnable {

    private static final String TAG = "DownloadImageTask";
    private static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000;
    private static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000;
    private static final int DEFAULT_BUFFER_SIZE = 2 * 1024;
    private static final String LOG_INFO_SUCCESS = "the image has downloaded successfully";
    private boolean mDownloading;

    // download state callback
    private DownloadProgressListener mListener;

    // Image resource url
    private String mUrl;

    public DownloadImageTask(String url, DownloadProgressListener listener) {
        this.mUrl = url;
        this.mListener = listener;
    }

    public boolean isDownloading() {
        return mDownloading;
    }

    @Override
    public void run() {
        Bitmap bitmap = downloadImage();
        if (bitmap != null) {
            mListener.onComplete(mUrl, bitmap);
        } else  {
            mListener.onFailed(new NullPointerException("Failed to convert as a bitmap"));
        }

    }

    /**
     * download image from network.
     * @return bitmap result
     */
    private Bitmap downloadImage() {
        mDownloading = true;
        byte[] ob = new byte[0];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = getStreamFromNetwork(mUrl)) {
            int current = 0;
            int total = inputStream.available();
            final byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            int count;
            while ((count = inputStream.read(bytes, 0, DEFAULT_BUFFER_SIZE)) != -1) {
                outputStream.write(bytes, 0, count);
                current += count;
                if (mListener != null) {
                    mListener.onProgressUpdate(current, total);
                }
            }
            ob = outputStream.toByteArray();
            IoUtils.closeSilently(inputStream);
            IoUtils.closeSilently(outputStream);

        } catch (IOException e) {
            e.printStackTrace();
            mListener.onFailed(e);
            IoUtils.closeSilently(outputStream);
        }
        mDownloading = false;
        if (ob.length != 0) {
            Log.d(TAG, LOG_INFO_SUCCESS);
            return BitmapFactory.decodeByteArray(ob, 0, ob.length);
        }
        return null;
    }

    /**
     * Fetch stream from network.
     * @param url url address
     * @return  stream data
     * @throws IOException exception throws
     */
    private InputStream getStreamFromNetwork(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
        conn.setReadTimeout(DEFAULT_HTTP_READ_TIMEOUT);
        InputStream imageStream;
        try {
            imageStream = conn.getInputStream();
        } catch (IOException e) {
            if (conn.getErrorStream() != null) {
                IoUtils.readAndCloseStream(conn.getErrorStream());
            }
            throw e;
        }

        if (conn.getResponseCode() != 200) {
            IoUtils.closeSilently(imageStream);
            throw new IOException("Network image request failed, response code: " + conn.getResponseCode());
        }
        return new ContentLengthInputStream(imageStream, conn.getContentLength());
    }
}
