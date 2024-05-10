package com.fastchar.servlet.http.cache;

import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.servlet.http.wrapper.FastHttpServletResponseWrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FastOutStreamCache extends OutputStream {

    private FastHttpServletResponse response;

    private final OutputStream outputStream;
    private boolean cache = true;

    private final List<Integer> bytes = new ArrayList<>();

    public FastOutStreamCache(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        if (isUseCache()) {
            bytes.add(b);
        }
    }

    private boolean isUseCache() {
        if (response != null && response instanceof FastHttpServletResponseWrapper) {
            FastHttpServletResponseWrapper responseWrapper = (FastHttpServletResponseWrapper) response;
            FastResponseCacheConfig cacheConfig = responseWrapper.getCacheConfig();
            return cacheConfig != null && cacheConfig.isCache();
        }
        return false;
    }


    public void saveCache() {
        try {
            if (response != null && response instanceof FastHttpServletResponseWrapper) {
                FastHttpServletResponseWrapper responseWrapper = (FastHttpServletResponseWrapper) response;
                FastResponseCacheConfig cacheConfig = responseWrapper.getCacheConfig();
                if (cacheConfig != null && cacheConfig.isCache()) {
                    try {
                        FastResponseCacheInfo cacheInfo = responseWrapper.getCacheInfo();
                        cacheInfo.getBytes().addAll(bytes);
                        cacheInfo.setTimeout(cacheConfig.getTimeout());
                        cacheInfo.setTimestamp(System.currentTimeMillis());
                        FastChar.getCache().set(cacheConfig.getCacheTag(), cacheConfig.getCacheKey(), cacheInfo);
                    } catch (Exception e) {
                        FastChar.getLogger().error(this.getClass(), e);
                    }
                }
            }
        } finally {
            bytes.clear();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        outputStream.close();
        saveCache();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        outputStream.flush();
    }



    public boolean isCache() {
        return cache;
    }

    public FastOutStreamCache setCache(boolean cache) {
        this.cache = cache;
        return this;
    }


    public FastHttpServletResponse getResponse() {
        return response;
    }

    public FastOutStreamCache setResponse(FastHttpServletResponse response) {
        this.response = response;
        return this;
    }
}
