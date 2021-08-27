package com.fastchar.response;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

public class FastResponseWrapper extends HttpServletResponseWrapper {
    private static final ThreadLocal<FastResponseCacheConfig> CacheKey = new ThreadLocal<>();

    public static void setCacheInfo(FastResponseCacheConfig responseCache) {
        CacheKey.set(responseCache);
    }

    public static FastResponseCacheConfig getCacheConfig() {
        return CacheKey.get();
    }

    private FastPrintWriterWrapper printWriter;
    private FastOutStreamWrapper outStreamWrapper;
    private final FastResponseCacheInfo cacheInfo = new FastResponseCacheInfo();
    public FastResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (printWriter == null) {
            printWriter = new FastPrintWriterWrapper(getOutputStream());
        }
        printWriter.setResponse(this);
        return printWriter;
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outStreamWrapper == null) {
            outStreamWrapper = new FastOutStreamWrapper();
        }
        outStreamWrapper.setOutputStream(this, super.getOutputStream());
        return outStreamWrapper;
    }

    @Override
    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        for (FastResponseHeader fastResponseHeader : cacheInfo.getDateHeader()) {
            if (fastResponseHeader.getName().equals(name)) {
                fastResponseHeader.setValue(date);
                return;
            }
        }
        FastResponseHeader fastResponseHeader = new FastResponseHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(date);
        cacheInfo.getDateHeader().add(fastResponseHeader);
    }

    @Override
    public void addDateHeader(String name, long date) {
        super.addDateHeader(name, date);
        FastResponseHeader fastResponseHeader = new FastResponseHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(date);
        cacheInfo.getDateHeader().add(fastResponseHeader);
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        for (FastResponseHeader fastResponseHeader : cacheInfo.getHeader()) {
            if (fastResponseHeader.getName().equals(name)) {
                fastResponseHeader.setValue(value);
                return;
            }
        }
        FastResponseHeader fastResponseHeader = new FastResponseHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(value);
        cacheInfo.getHeader().add(fastResponseHeader);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        FastResponseHeader fastResponseHeader = new FastResponseHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(value);
        cacheInfo.getHeader().add(fastResponseHeader);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        for (FastResponseHeader fastResponseHeader : cacheInfo.getIntHeader()) {
            if (fastResponseHeader.getName().equals(name)) {
                fastResponseHeader.setValue(value);
                return;
            }
        }
        FastResponseHeader fastResponseHeader = new FastResponseHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(value);
        cacheInfo.getIntHeader().add(fastResponseHeader);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        FastResponseHeader fastResponseHeader = new FastResponseHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(value);
        cacheInfo.getIntHeader().add(fastResponseHeader);
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        cacheInfo.setStatus(sc);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        super.setCharacterEncoding(charset);
        cacheInfo.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        super.setContentLength(len);
        cacheInfo.setContentLength(len);
    }

    @Override
    public void setContentType(String type) {
        super.setContentType(type);
        cacheInfo.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {
        super.setBufferSize(size);
        cacheInfo.setBufferSize(size);
    }

    public FastResponseCacheInfo getCacheInfo() {
        return cacheInfo;
    }

}

