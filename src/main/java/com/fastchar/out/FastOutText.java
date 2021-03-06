package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.utils.FastFileUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 响应输出文本
 */
public class FastOutText extends FastOut<FastOutText> {

    public FastOutText() {
        this.contentType = "text/plain";
    }


    @Override
    public void response(FastAction action) throws Exception {
        HttpServletResponse response = action.getResponse();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setStatus(getStatus());
        response.setContentType(toContentType(action));
        response.setCharacterEncoding(getCharset());

        try (PrintWriter writer = response.getWriter()) {
            if (data instanceof File) {
                File htmlFile = (File) data;
                writer.write(FastFileUtils.readFileToString(htmlFile,FastChar.getConstant().getEncoding()));
            }else{
                writer.write(String.valueOf(data));
            }
            writer.flush();
        }

    }
}


