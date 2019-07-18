package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 响应Html
 */
public class FastOutHtml extends FastOut<FastOutHtml> {

    public FastOutHtml() {
        this.contentType = "text/html";
    }

    @Override
    public void response(FastAction action) throws Exception {
        HttpServletResponse response = action.getResponse();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setStatus(getStatus());

        response.setContentType(toContentType());
        response.setCharacterEncoding(getCharset());

        PrintWriter writer = response.getWriter();
        writer.write(String.valueOf(data));
        writer.flush();
    }
}
