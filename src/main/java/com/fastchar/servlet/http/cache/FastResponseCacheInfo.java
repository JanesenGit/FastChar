package com.fastchar.servlet.http.cache;

import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.servlet.http.wrapper.FastHttpServletResponseWrapper;
import com.fastchar.utils.FastNumberUtils;
import org.apache.catalina.connector.ClientAbortException;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FastResponseCacheInfo implements Serializable {

    private int status = 200;
    private String contentType;
    private int contentLength = -1;
    private String characterEncoding;
    private int bufferSize = -1;
    private List<FastResponseCacheHeader> dateHeader = new ArrayList<>(16);
    private List<FastResponseCacheHeader> intHeader = new ArrayList<>(16);
    private List<FastResponseCacheHeader> header = new ArrayList<>(16);
//    private final StringBuilder data = new StringBuilder();

    private final List<Integer> bytes = new ArrayList<>();

//    private int cacheType;//0 字符串 1 字节

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


//    public String getData() {
//        return data.toString();
//    }
//
//    public FastResponseCacheInfo setData(String data) {
//        this.data.setLength(0);
//        this.data.append(data);
//        return this;
//    }
//
//    public FastResponseCacheInfo addData(String data) {
//        this.data.append(data);
//        return this;
//    }

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

//    public int getCacheType() {
//        return cacheType;
//    }
//
//    public FastResponseCacheInfo setCacheType(int cacheType) {
//        this.cacheType = cacheType;
//        return this;
//    }

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
            response.setDateHeader(fastResponseHeader.getName(), FastNumberUtils.formatToLong(fastResponseHeader.getValue()));
        }

        for (FastResponseCacheHeader fastResponseHeader : intHeader) {
            response.setIntHeader(fastResponseHeader.getName(), FastNumberUtils.formatToInt(fastResponseHeader.getValue()));
        }
        for (FastResponseCacheHeader fastResponseHeader : header) {
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
            } catch (ClientAbortException ignored) {
                //这个异常是由于客户端断开连接，可以忽略
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (FastChar.getConstant().isDebug()) {
                FastChar.getLog().info(this.getClass(), "Cached:" + request.getRequestURI());
            }
        }


    }


}
