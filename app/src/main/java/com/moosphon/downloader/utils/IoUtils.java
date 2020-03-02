package com.moosphon.downloader.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class IoUtils {
    private static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32kb

    /**
     * Reads all data from stream and close it silently
     *
     * @param is Input stream
     */
    public static void readAndCloseStream(InputStream is) {
        final byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
        try {
            while (is.read(bytes, 0, DEFAULT_BUFFER_SIZE) != -1);
        } catch (IOException ignored) {
        } finally {
            closeSilently(is);
        }
    }

    /**
     * Close stream.
     * @param closeable target stream.
     */
    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
