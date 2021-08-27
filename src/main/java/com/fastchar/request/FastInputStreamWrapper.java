package com.fastchar.request;

import com.fastchar.utils.FastStringUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author 沈建（Janesen）
 * @date 2021/8/24 15:54
 */
public class FastInputStreamWrapper extends ServletInputStream {

    private InputStream inputStream;

    void setServletInputStream(HttpServletRequest request, ServletInputStream servletInputStream) throws IOException {
        String contentEncoding = FastStringUtils.defaultValue(request.getHeader("Content-Encoding"), "");
        if (contentEncoding.contains("gzip")) {
            this.inputStream = new GZIPInputStream(servletInputStream, 1024 * 6);
        } else {
            this.inputStream = servletInputStream;
        }
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

}
