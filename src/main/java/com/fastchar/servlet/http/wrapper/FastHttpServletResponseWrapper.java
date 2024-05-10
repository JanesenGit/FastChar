package com.fastchar.servlet.http.wrapper;


import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.servlet.http.cache.FastOutStreamCache;
import com.fastchar.servlet.http.cache.FastResponseCacheConfig;
import com.fastchar.servlet.http.cache.FastResponseCacheHeader;
import com.fastchar.servlet.http.cache.FastResponseCacheInfo;

import java.io.IOException;
import java.io.OutputStream;

public class FastHttpServletResponseWrapper extends FastHttpServletResponse {
    public static FastHttpServletResponseWrapper newInstance(Object target) {
        return new FastHttpServletResponseWrapper(target);
    }


    private FastOutStreamCache outStreamCache;

    private FastResponseCacheConfig cacheConfig;
    private final FastResponseCacheInfo cacheInfo = new FastResponseCacheInfo();

    public FastHttpServletResponseWrapper(Object target) {
        super(target);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outStreamCache == null) {
            outStreamCache = new FastOutStreamCache(super.getOutputStream());
        }
        outStreamCache.setResponse(this);
        return outStreamCache;
    }

    public OutputStream getBaseOutputStream() throws IOException {
        return super.getOutputStream();
    }

    @Override
    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        for (FastResponseCacheHeader fastResponseHeader : cacheInfo.getDateHeader()) {
            if (fastResponseHeader.getName().equals(name)) {
                fastResponseHeader.setValue(date);
                return;
            }
        }
        FastResponseCacheHeader fastResponseHeader = new FastResponseCacheHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(date);
        cacheInfo.getDateHeader().add(fastResponseHeader);
    }

    @Override
    public void addDateHeader(String name, long date) {
        super.addDateHeader(name, date);
        FastResponseCacheHeader fastResponseHeader = new FastResponseCacheHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(date);
        cacheInfo.getDateHeader().add(fastResponseHeader);
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        if (cacheInfo.isExcludeHead(name)) {
            return;
        }
        for (FastResponseCacheHeader fastResponseHeader : cacheInfo.getHeader()) {
            if (fastResponseHeader.getName().equals(name)) {
                fastResponseHeader.setValue(value);
                return;
            }
        }
        FastResponseCacheHeader fastResponseHeader = new FastResponseCacheHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(value);
        cacheInfo.getHeader().add(fastResponseHeader);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        if (cacheInfo.isExcludeHead(name)) {
            return;
        }
        FastResponseCacheHeader fastResponseHeader = new FastResponseCacheHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(value);
        cacheInfo.getHeader().add(fastResponseHeader);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        if (cacheInfo.isExcludeHead(name)) {
            return;
        }
        for (FastResponseCacheHeader fastResponseHeader : cacheInfo.getIntHeader()) {
            if (fastResponseHeader.getName().equals(name)) {
                fastResponseHeader.setValue(value);
                return;
            }
        }
        FastResponseCacheHeader fastResponseHeader = new FastResponseCacheHeader();
        fastResponseHeader.setName(name);
        fastResponseHeader.setValue(value);
        cacheInfo.getIntHeader().add(fastResponseHeader);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        if (cacheInfo.isExcludeHead(name)) {
            return;
        }
        FastResponseCacheHeader fastResponseHeader = new FastResponseCacheHeader();
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

    public FastResponseCacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public FastHttpServletResponseWrapper setCacheConfig(FastResponseCacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        return this;
    }
}

