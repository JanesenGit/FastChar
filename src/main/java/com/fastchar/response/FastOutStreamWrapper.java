package com.fastchar.response;

import com.fastchar.utils.FastStringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author 沈建（Janesen）
 * @date 2021/8/24 13:39
 */
class FastOutStreamWrapper extends ServletOutputStream {
    private OutputStream outputStream;

    void setOutputStream(HttpServletResponse response, ServletOutputStream outputStream) throws IOException {
        String contentEncoding = FastStringUtils.defaultValue(response.getHeader("Content-Encoding"), "");
        if (contentEncoding.contains("gzip")) {
            this.outputStream = new GZIPOutputStream(outputStream, 1024 * 6);
        } else {
            this.outputStream = outputStream;
        }
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }


    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }


}
