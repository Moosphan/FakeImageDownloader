package com.moosphon.downloader.task;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provide possibility to get target stream length by {@link #available()}.
 */
public class ContentLengthInputStream extends InputStream {

    private final InputStream stream;
    private final int length;

    public ContentLengthInputStream(InputStream stream, int length) {
        this.stream = stream;
        this.length = length;
    }

    @Override
    public int available() throws IOException {
        return length;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return stream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
        stream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return stream.skip(n);
    }

    public InputStream getStream() {
        return stream;
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    @Override
    public void mark(int readLimit) {
        stream.mark(readLimit);
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
