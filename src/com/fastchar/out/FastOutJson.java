package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastProviderException;
import com.fastchar.interfaces.IFastJsonProvider;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

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
        response.setContentType(toContentType());
        response.setCharacterEncoding(getCharset());
        PrintWriter writer = response.getWriter();
        writer.write(FastChar.getJson().toJson(data));
        writer.flush();
    }

}
