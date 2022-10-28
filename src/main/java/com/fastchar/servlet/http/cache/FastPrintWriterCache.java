package com.fastchar.servlet.http.cache;

import com.fastchar.servlet.http.FastHttpServletResponse;

import java.io.OutputStream;
import java.io.PrintWriter;

public class FastPrintWriterCache extends PrintWriter {
    private FastHttpServletResponse response;
    private boolean cache = true;

    public FastPrintWriterCache(OutputStream out) {
        super(out);
    }

    public FastPrintWriterCache setResponse(FastHttpServletResponse response) {
        this.response = response;
        return this;
    }

//
//    @Override
//    public void write(String s) {
//        super.write(s);
//        if (response != null && response instanceof FastHttpServletResponseWrapper && cache) {
//            FastHttpServletResponseWrapper responseWrapper = (FastHttpServletResponseWrapper) response;
//            FastResponseCacheConfig cacheConfig = responseWrapper.getCacheConfig();
//            if (cacheConfig != null && cacheConfig.isCache()) {
//                try {
//                    FastResponseCacheInfo cacheInfo = responseWrapper.getCacheInfo();
//                    cacheInfo.addData(s);
//                    cacheInfo.setTimeout(cacheConfig.getTimeout());
//                    cacheInfo.setTimestamp(System.currentTimeMillis());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private void saveCache() {
//        try {
//            if (response != null && response instanceof FastHttpServletResponseWrapper && cache) {
//                FastHttpServletResponseWrapper responseWrapper = (FastHttpServletResponseWrapper) response;
//                FastResponseCacheConfig cacheConfig = responseWrapper.getCacheConfig();
//                if (cacheConfig != null && cacheConfig.isCache()) {
//                    FastResponseCacheInfo cacheInfo = responseWrapper.getCacheInfo();
//                    cacheInfo.setCacheType(0);
//                    FastChar.getCache().set(cacheConfig.getCacheTag(), cacheConfig.getCacheKey(), cacheInfo);
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void close() {
//        super.close();
//        saveCache();
//    }

    public boolean isCache() {
        return cache;
    }

    public FastPrintWriterCache setCache(boolean cache) {
        this.cache = cache;
        return this;
    }
}
