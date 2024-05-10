package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.utils.FastFileUtils;

import java.io.File;

/**
 * 响应Html
 */
public class FastOutHtml extends FastOut<FastOutHtml> {

    public FastOutHtml() {
        this.contentType = "text/html";
    }

    @Override
    public void response(FastAction action) throws Exception {
        FastHttpServletResponse response = action.getResponse();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);


        response.setStatus(getStatus());
        response.setContentType(toContentType(action, false));
        response.setCharacterEncoding(getCharset());

        if (data instanceof File) {
            File htmlFile = (File) data;
            write(response,FastFileUtils.readFileToString(htmlFile, FastChar.getConstant().getCharset()));
        }else{
            write(response,String.valueOf(data));
        }
    }
}
