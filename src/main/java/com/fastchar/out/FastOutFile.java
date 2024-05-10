package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastResource;
import com.fastchar.exception.FastFileException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.*;
import java.net.*;

/**
 * 响应文件流，下载文件
 */
public class FastOutFile extends FastOut<FastOutFile> {

    private String fileName;
    private boolean disposition = true;
    private int inputSize = 1024 * 4;
    private int outputSize = 1024 * 4;


    public FastOutFile() {
        this.contentType = "application/octet-stream";
    }

    public String getFileName() {
        return fileName;
    }

    public FastOutFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public boolean isDisposition() {
        return disposition;
    }

    public FastOutFile setDisposition(boolean disposition) {
        this.disposition = disposition;
        return this;
    }

    public int getInputSize() {
        return inputSize;
    }

    public FastOutFile setInputSize(int inputSize) {
        this.inputSize = inputSize;
        return this;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public FastOutFile setOutputSize(int outputSize) {
        this.outputSize = outputSize;
        return this;
    }

    @Override
    public void response(FastAction action) throws Exception {
        FastResource resource = parseResource();
        if (resource == null) {
            throw new NullPointerException("resource data is null");
        }

        if (resource.isFileProtocol() && !resource.getFile().exists()) {
            throw new FastFileException(FastChar.getLocal().getInfo(FastCharLocal.FILE_ERROR7, "'" + resource.getFile().getAbsolutePath() + "'"));
        }

        this.setDescription("response file '" + resource.getURL().toString() + "' ");
        FastHttpServletResponse response = action.getResponse();
        response.setHeader("Accept-Ranges", "bytes");
        if (FastStringUtils.isEmpty(fileName)) {
            fileName = resource.getName();
        }
        if (disposition) {
            response.setHeader("Content-disposition", "attachment; " + encodeFileName(action.getRequest(), fileName));
        }
        this.contentType = action.getServletContext().getMimeType(resource.getName());
        if (FastStringUtils.isEmpty(this.contentType)) {
            this.contentType = "application/octet-stream";
        }
        response.setContentType(this.contentType);
        response.setBufferSize(outputSize);
        URLConnection urlConnection = resource.getURL().openConnection();
        if (FastStringUtils.isEmpty(action.getRequest().getHeader("Range"))) {
            responseAllFile(response, urlConnection);
        } else {
            responseRangFile(action.getRequest(), response, urlConnection);
        }

    }

    private FastResource parseResource() throws MalformedURLException {
        FastResource resource = null;
        if (data instanceof File) {
            resource = new FastResource((File) data);
        } else if (data instanceof String) {
            resource = new FastResource((String) data);
        } else if (data instanceof URL) {
            resource = new FastResource((URL) data);
        } else if (data instanceof URI) {
            resource = new FastResource(((URI) data).toURL());
        } else if (data instanceof FastResource) {
            resource = ((FastResource) data);
        }
        return resource;
    }


    private String encodeFileName(FastHttpServletRequest request, String fileName) {
        String userAgent = request.getHeader("User-Agent");
        try {
            String encodedFileName = URLEncoder.encode(fileName, "utf-8");
            if (userAgent == null) {
                return "filename=\"" + encodedFileName + "\"";
            }
            userAgent = userAgent.toLowerCase();
            if (userAgent.contains("mozilla") || userAgent.contains("safari") || userAgent.contains("applewebkit") || userAgent.contains("opera")) {
                return "filename*=UTF-8''" + encodedFileName;
            }
            return "filename=\"" + encodedFileName + "\"";
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("IOStreamConstructor")
    private void responseAllFile(FastHttpServletResponse response, URLConnection urlConnection) {
        response.setContentLength(urlConnection.getContentLength());
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            write(response, inputStream, inputSize);
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        } finally {
            FastFileUtils.closeQuietly(inputStream);
        }
    }


    @SuppressWarnings("IOStreamConstructor")
    private void responseRangFile(FastHttpServletRequest request, FastHttpServletResponse response, URLConnection urlConnection) {
        Long[] range = {null, null};
        processRange(request, urlConnection, range);
        response.setContentLength((int) (range[1] - range[0] + 1));
        response.setStatus(FastHttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Range", "bytes " + range[0] + "-" + range[1] + "/" + urlConnection.getContentLength());
        try {
            long start = range[0];
            long end = range[1];
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            write(response, inputStream, inputSize, start, end);
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

    private void processRange(FastHttpServletRequest request, URLConnection urlConnection, Long[] range) {
        String rangeStr = request.getHeader("Range");
        int index = rangeStr.indexOf(',');
        if (index != -1) {
            rangeStr = rangeStr.substring(0, index);
        }
        rangeStr = rangeStr.replace("bytes=", "");

        String[] arr = rangeStr.split("-", 2);
        if (arr.length < 2) {
            throw new RuntimeException("Range error");
        }

        long fileLength = urlConnection.getContentLength();
        for (int i = 0; i < range.length; i++) {
            if (FastStringUtils.isNotBlank(arr[i])) {
                range[i] = Long.parseLong(arr[i].trim());
                if (range[i] >= fileLength) {
                    range[i] = fileLength - 1;
                }
            }
        }
        if (range[0] != null && range[1] == null) {
            range[1] = fileLength - 1;
        } else if (range[0] == null && range[1] != null) {
            range[0] = fileLength - range[1];
            range[1] = fileLength - 1;
        }

        if (range[0] == null || range[0] > range[1]) {
            throw new RuntimeException("Range error");
        }
    }


}
