package com.fastchar.out;

import com.fastchar.core.FastAction;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 响应输出xml
 */
public class FastOutXml extends FastOut<FastOutXml> {
    public FastOutXml() {
        this.contentType = "text/xml";
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
