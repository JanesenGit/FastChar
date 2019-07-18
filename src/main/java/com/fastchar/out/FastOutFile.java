package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastFileException;

import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastStringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 响应文件流，下载文件
 */
public class FastOutFile extends FastOut<FastOutFile> {

    private String fileName;
    private boolean disposition = true;
    private int inputSize = 2048;
    private int outputSize = 2048;


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
        response.setBufferSize(outputSize);
        if (FastStringUtils.isEmpty(action.getRequest().getHeader("Range"))) {
            responseAllFile(response, file);
        } else {
            responseRangFile(action.getRequest(), response, file);
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

    private void responseAllFile(HttpServletResponse response , File file) {
        response.setContentLength((int) file.length());
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            ServletOutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[inputSize];
            int len;
            while (true) {
                try {
                    len = inputStream.read(buffer);
                    if (len == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, len);
                } catch (IOException e) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FastFileUtils.closeQuietly(inputStream);
        }
    }


    private void responseRangFile(HttpServletRequest request, HttpServletResponse response, File file) {
        Long[] range = {null, null};
        processRange(request, file, range);

        response.setContentLength((int) (range[1] - range[0] + 1));
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

        response.setHeader("Content-Range", "bytes " + range[0] + "-" + range[1] + "/" + file.length());

        InputStream inputStream = null;
        try {
            long start = range[0];
            long end = range[1];
            inputStream = new BufferedInputStream(new FileInputStream(file));
            if (inputStream.skip(start) != start)
                throw new RuntimeException("File skip error");
            ServletOutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[inputSize];
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FastFileUtils.closeQuietly(inputStream);
        }
    }

    private void processRange(HttpServletRequest request, File file, Long[] range) {
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
