package com.fastchar.response;

import com.fastchar.core.FastChar;
import com.fastchar.utils.FastNumberUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

public class FastResponseCacheInfo implements Serializable {

    private int status = 200;
    private String contentType;
    private int contentLength = -1;
    private String characterEncoding;
    private int bufferSize = -1;
    private List<FastResponseHeader> dateHeader = new ArrayList<>();
    private List<FastResponseHeader> intHeader = new ArrayList<>();
    private List<FastResponseHeader> header = new ArrayList<>();
    private String data;
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


    public String getData() {
        return data;
    }

    public FastResponseCacheInfo setData(String data) {
        this.data = data;
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

    public List<FastResponseHeader> getDateHeader() {
        return dateHeader;
    }

    public FastResponseCacheInfo setDateHeader(List<FastResponseHeader> dateHeader) {
        this.dateHeader = dateHeader;
        return this;
    }

    public List<FastResponseHeader> getIntHeader() {
        return intHeader;
    }

    public FastResponseCacheInfo setIntHeader(List<FastResponseHeader> intHeader) {
        this.intHeader = intHeader;
        return this;
    }

    public List<FastResponseHeader> getHeader() {
        return header;
    }

    public FastResponseCacheInfo setHeader(List<FastResponseHeader> header) {
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


    public void response(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setStatus(this.status);
            response.setContentType(this.contentType);
            response.setCharacterEncoding(this.characterEncoding);
            if (this.contentLength != -1) {
                response.setContentLength(this.contentLength);
            }
            if (this.bufferSize != -1) {
                response.setBufferSize(this.bufferSize);
            }
            for (FastResponseHeader fastResponseHeader : dateHeader) {
                response.setDateHeader(fastResponseHeader.getName(), FastNumberUtils.formatToLong(fastResponseHeader.getValue()));
            }

            for (FastResponseHeader fastResponseHeader : intHeader) {
                response.setIntHeader(fastResponseHeader.getName(), FastNumberUtils.formatToInt(fastResponseHeader.getValue()));
            }
            for (FastResponseHeader fastResponseHeader : header) {
                response.setHeader(fastResponseHeader.getName(), String.valueOf(fastResponseHeader.getValue()));
            }

            PrintWriter writer = response.getWriter();
            if (writer instanceof FastPrintWriterWrapper) {
                FastPrintWriterWrapper writerWrapper = (FastPrintWriterWrapper) writer;
                writerWrapper.setCache(false);
            }
            writer.write(this.data);
            writer.flush();
            FastChar.getLog().info("Cached:" + request.getRequestURI());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
