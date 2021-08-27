package com.fastchar.response;

import com.fastchar.core.FastChar;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

class FastPrintWriterWrapper extends PrintWriter {
    private HttpServletResponse response;
    private boolean cache = true;


    public FastPrintWriterWrapper(OutputStream out) {
        super(out);
    }

    FastPrintWriterWrapper setResponse(HttpServletResponse response) {
        this.response = response;
        return this;
    }

    @Override
    public void write(String s) {
        super.write(s);
        if (response != null && response instanceof FastResponseWrapper && cache) {
            FastResponseCacheConfig cacheConfig = FastResponseWrapper.getCacheConfig();
            if (cacheConfig != null && cacheConfig.isCache()) {
                try {
                    FastResponseWrapper wrapper = (FastResponseWrapper) response;
                    FastResponseCacheInfo cacheInfo = wrapper.getCacheInfo();
                    cacheInfo.setData(s);
                    cacheInfo.setTimeout(cacheConfig.getTimeout());
                    cacheInfo.setTimestamp(System.currentTimeMillis());
                    FastChar.getCache().set(cacheConfig.getCacheTag(), cacheConfig.getCacheKey(), cacheInfo);
                } catch (Exception ignored) {
                }
            }
        }
    }


    public boolean isCache() {
        return cache;
    }

    public FastPrintWriterWrapper setCache(boolean cache) {
        this.cache = cache;
        return this;
    }
}
