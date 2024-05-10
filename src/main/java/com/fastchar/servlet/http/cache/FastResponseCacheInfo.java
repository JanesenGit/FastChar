package com.fastchar.servlet.http.cache;

import com.fastchar.core.FastChar;
import com.fastchar.servlet.FastHttpHeaders;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.servlet.http.wrapper.FastHttpServletResponseWrapper;
import com.fastchar.utils.FastNumberUtils;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FastResponseCacheInfo implements Serializable {

    public static final String[] EXCLUDE_CACHE_HEAD = new String[]{
            FastHttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
            FastHttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
            FastHttpHeaders.ACCESS_CONTROL_MAX_AGE,
            FastHttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
            FastHttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
    };


    private int status = 200;
    private String contentType;
    private int contentLength = -1;
    private String characterEncoding;
    private int bufferSize = -1;
    private List<FastResponseCacheHeader> dateHeader = new ArrayList<>(16);
    private List<FastResponseCacheHeader> intHeader = new ArrayList<>(16);
    private List<FastResponseCacheHeader> header = new ArrayList<>(16);

    private final List<Integer> bytes = new ArrayList<>();

    private long timeout;
    private long timestamp;

    public int getStatus() {
        return status;
    }

    public FastResponseCacheInfo setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public FastResponseCacheInfo setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public FastResponseCacheInfo setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        return this;
    }

    public int getContentLength() {
        return contentLength;
    }

    public FastResponseCacheInfo setContentLength(int contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public FastResponseCacheInfo setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public List<FastResponseCacheHeader> getDateHeader() {
        return dateHeader;
    }

    public FastResponseCacheInfo setDateHeader(List<FastResponseCacheHeader> dateHeader) {
        this.dateHeader = dateHeader;
        return this;
    }

    public List<FastResponseCacheHeader> getIntHeader() {
        return intHeader;
    }

    public FastResponseCacheInfo setIntHeader(List<FastResponseCacheHeader> intHeader) {
        this.intHeader = intHeader;
        return this;
    }

    public List<FastResponseCacheHeader> getHeader() {
        return header;
    }

    public FastResponseCacheInfo setHeader(List<FastResponseCacheHeader> header) {
        this.header = header;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public FastResponseCacheInfo setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public long getTimeout() {
        return timeout;
    }

    public FastResponseCacheInfo setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }


    public boolean isTimeout() {
        if (timeout < 0) {
            return false;
        }
        long l = (System.currentTimeMillis() - timestamp) / 1000;
        return l > timeout;
    }


    public List<Integer> getBytes() {
        return bytes;
    }


    public boolean isExcludeHead(String headName) {
        for (String head : EXCLUDE_CACHE_HEAD) {
            if (head.equalsIgnoreCase(headName)) {
                return true;
            }
        }
        return false;
    }


    public void response(FastHttpServletRequest request, FastHttpServletResponse response) {
        response.setStatus(this.status);
        response.setContentType(this.contentType);
        response.setCharacterEncoding(this.characterEncoding);
        if (this.contentLength != -1) {
            response.setContentLength(this.contentLength);
        }
        if (this.bufferSize != -1) {
            response.setBufferSize(this.bufferSize);
        }
        for (FastResponseCacheHeader fastResponseHeader : dateHeader) {
            if (isExcludeHead(fastResponseHeader.getName())) {
                continue;
            }
            response.setDateHeader(fastResponseHeader.getName(), FastNumberUtils.formatToLong(fastResponseHeader.getValue()));
        }

        for (FastResponseCacheHeader fastResponseHeader : intHeader) {
            if (isExcludeHead(fastResponseHeader.getName())) {
                continue;
            }
            response.setIntHeader(fastResponseHeader.getName(), FastNumberUtils.formatToInt(fastResponseHeader.getValue()));
        }
        for (FastResponseCacheHeader fastResponseHeader : header) {
            if (isExcludeHead(fastResponseHeader.getName())) {
                continue;
            }
            response.setHeader(fastResponseHeader.getName(), String.valueOf(fastResponseHeader.getValue()));
        }
        response.addHeader("FastChar-Cached", "true");

        if (response instanceof FastHttpServletResponseWrapper) {
            FastHttpServletResponseWrapper httpServletResponseWrapper = (FastHttpServletResponseWrapper) response;
            try (OutputStream streamWriter = httpServletResponseWrapper.getBaseOutputStream()) {
                for (Integer b : bytes) {
                    streamWriter.write(b);
                }
                streamWriter.flush();
            } catch (Exception e) {
                if (e.getClass().getSimpleName().equalsIgnoreCase("ClientAbortException")) {
                    //这个异常是由于客户端断开连接，可以忽略
                    return;
                }
                FastChar.getLogger().error(this.getClass(), e);
            }
            if (FastChar.getConstant().isDebug()) {
                FastChar.getLogger().info(this.getClass(), "Cached:" + request.getRequestURI());
            }
        }
    }


}
