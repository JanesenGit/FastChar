package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastStringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;

/**
 * 响应json
 */
public class FastOutJson extends FastOut<FastOutJson> {

    public FastOutJson() {
        this.contentType = "application/json";
    }

    @Override
    public void response(FastAction action) throws Exception {
        HttpServletResponse response = action.getResponse();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setStatus(getStatus());

        String accept = action.getRequest().getHeader("accept");
        if (FastStringUtils.isNotEmpty(accept)) {
            if (accept.contains("text/html".toLowerCase())) {//浏览器要求返回text/html格式的json，一般是ie浏览器上传文件时会发生
                this.contentType = "text/html";
            }
        }
        response.setContentType(toContentType(action));
        response.setCharacterEncoding(getCharset());
        try (PrintWriter writer = response.getWriter()) {
            if (data instanceof File) {
                String jsonData = FastFileUtils.readFileToString((File) data, FastChar.getConstant().getEncoding());
                writer.write(jsonData);
            } else {
                writer.write(FastChar.getJson().toJson(data));
            }
            writer.flush();
        }
    }

}
