package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.utils.FastFileUtils;

import java.io.File;

/**
 * 响应json
 */
public class FastOutJson extends FastOut<FastOutJson> {

    public FastOutJson() {
        this.contentType = "application/json";
    }

    @Override
    public void response(FastAction action) throws Exception {
        FastHttpServletResponse response = action.getResponse();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setStatus(getStatus());


        response.setContentType(toContentType(action));
        response.setCharacterEncoding(getCharset());
        if (data instanceof File) {
            String jsonData = FastFileUtils.readFileToString((File) data, FastChar.getConstant().getCharset());
            write(response,jsonData);
        } else {
            write(response, FastChar.getJson().toJson(data));
        }
    }

}
