package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastFileException;

import com.fastchar.utils.FastStringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FastOutFile extends FastOut<FastOutFile> {

    private String fileName;
    private boolean disposition = true;

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


    @Override
    public void response(FastAction action) throws Exception {
        File file;
        if (data instanceof File) {
            file= (File) data;
        }else{
            file = new File(String.valueOf(data));
        }
        if (!file.exists()) {
            throw new FastFileException(FastChar.getLocal().getInfo("File_Error7", "'" + file.getAbsolutePath() + "'"));
        }

        if (!file.isFile()) {
            throw new FastFileException(FastChar.getLocal().getInfo("File_Error8", "'" + file.getAbsolutePath() + "'"));
        }

        this.setDescription("response file '" + file.getAbsolutePath() + "' ");
        HttpServletResponse response = action.getResponse();
        response.setHeader("Accept-Ranges", "bytes");
        if (FastStringUtils.isEmpty(fileName)) {
            fileName = file.getName();
        }
        if (disposition) {
            response.setHeader("Content-disposition", "attachment; " + encodeFileName(action.getRequest(), fileName));
        }
        this.contentType = action.getServletContext().getMimeType(file.getName());
        if (FastStringUtils.isEmpty(this.contentType)) {
            this.contentType = "application/octet-stream";
        }
        response.setContentType(this.contentType);

        if (FastStringUtils.isEmpty(action.getRequest().getHeader("Range"))) {
            responseAllFile(action, file);
        } else {
            responseRangFile(action, file);
        }

    }


    private String encodeFileName(HttpServletRequest request, String fileName) {
        String userAgent = request.getHeader("User-Agent");
        try {
            String encodedFileName = URLEncoder.encode(fileName, "UTF8");
            if (userAgent == null) {
                return "filename=\"" + encodedFileName + "\"";
            }
            userAgent = userAgent.toLowerCase();
            if (userAgent.contains("msie")) {
                return "filename=\"" + encodedFileName + "\"";
            }
            if (userAgent.contains("opera")) {
                return "filename*=UTF-8''" + encodedFileName;
            }
            if (userAgent.contains("safari") || userAgent.contains("applewebkit") || userAgent.contains("chrome")) {
                return "filename=\"" + new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1") + "\"";
            }
            if (userAgent.contains("mozilla")) {
                return "filename*=UTF-8''" + encodedFileName;
            }
            return "filename=\"" + encodedFileName + "\"";
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void responseAllFile(FastAction action, File file) {
        HttpServletResponse response = action.getResponse();
        response.setHeader("Content-Length", String.valueOf(file.length()));
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            for (int len = -1; (len = inputStream.read(buffer)) != -1; ) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (Exception ignored) {
                }
        }
    }


    private void responseRangFile(FastAction action, File file) {
        HttpServletResponse response = action.getResponse();
        Long[] range = {null, null};
        processRange(action, file, range);

        String contentLength = String.valueOf(range[1] - range[0] + 1);
        response.setHeader("Content-Length", contentLength);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

        response.setHeader("Content-Range", "bytes " + range[0] + "-" + range[1] + "/" + file.length());

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            long start = range[0];
            long end = range[1];
            inputStream = new BufferedInputStream(new FileInputStream(file));
            if (inputStream.skip(start) != start)
                throw new RuntimeException("File skip error");
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            long position = start;
            for (int len; position <= end && (len = inputStream.read(buffer)) != -1; ) {
                if (position + len <= end) {
                    outputStream.write(buffer, 0, len);
                    position += len;
                } else {
                    for (int i = 0; i < len && position <= end; i++) {
                        outputStream.write(buffer[i]);
                        position++;
                    }
                }
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (Exception ignored) {
                }
        }
    }

    private void processRange(FastAction action, File file, Long[] range) {
        HttpServletRequest request = action.getRequest();
        String rangeStr = request.getHeader("Range");
        int index = rangeStr.indexOf(',');
        if (index != -1)
            rangeStr = rangeStr.substring(0, index);
        rangeStr = rangeStr.replace("bytes=", "");

        String[] arr = rangeStr.split("-", 2);
        if (arr.length < 2)
            throw new RuntimeException("Range error");

        long fileLength = file.length();
        for (int i = 0; i < range.length; i++) {
            if (FastStringUtils.isNotBlank(arr[i])) {
                range[i] = Long.parseLong(arr[i].trim());
                if (range[i] >= fileLength)
                    range[i] = fileLength - 1;
            }
        }
        if (range[0] != null && range[1] == null) {
            range[1] = fileLength - 1;
        } else if (range[0] == null && range[1] != null) {
            range[0] = fileLength - range[1];
            range[1] = fileLength - 1;
        }

        if (range[0] == null || range[0] > range[1])
            throw new RuntimeException("Range error");
    }


}
