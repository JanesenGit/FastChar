package com.fastchar.response;

import com.fastchar.core.FastChar;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Locale;

public class FastPrintWriterWrapper extends PrintWriter {
    private PrintWriter printWriter;
    private HttpServletResponse response;
    private boolean cache = true;

    public FastPrintWriterWrapper(PrintWriter out) {
        super(out);
        this.printWriter = out;
    }

    FastPrintWriterWrapper setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
        return this;
    }

    public FastPrintWriterWrapper setResponse(HttpServletResponse response) {
        this.response = response;
        return this;
    }

    @Override
    public void flush() {
        printWriter.flush();
    }

    @Override
    public void close() {
        printWriter.close();
    }

    @Override
    public boolean checkError() {
        return printWriter.checkError();
    }


    @Override
    public void write(int c) {
        printWriter.write(c);
    }

    @Override
    public void write(char[] buf, int off, int len) {
        printWriter.write(buf, off, len);
    }

    @Override
    public void write(char[] buf) {
        printWriter.write(buf);
    }

    @Override
    public void write(String s) {
        printWriter.write(s);
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

    @Override
    public void write(String s, int off, int len) {
        printWriter.write(s, off, len);

    }

    @Override
    public void println() {
        printWriter.println();
    }


    @Override
    public PrintWriter printf(String format, Object... args) {
        return printWriter.printf(format, args);
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
        return printWriter.printf(l, format, args);
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        return printWriter.format(format, args);
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
        return printWriter.format(l, format, args);
    }


    public boolean isCache() {
        return cache;
    }

    public FastPrintWriterWrapper setCache(boolean cache) {
        this.cache = cache;
        return this;
    }
}
