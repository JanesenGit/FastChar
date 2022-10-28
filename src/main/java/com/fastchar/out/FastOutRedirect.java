package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastHttpServletResponse;

/**
 * 重定向请求
 */
public class FastOutRedirect extends FastOut<FastOutRedirect> {

    @Override
    public void response(FastAction action) throws Exception {
        String url = FastChar.wrapperUrl(String.valueOf(data));
        FastHttpServletResponse response = action.getResponse();
        response.setStatus(getStatus());
        if (getStatus() == 301) {
            this.setDescription("moved permanently  to '" + url + "'");
            response.setHeader("Location", url);
            response.setHeader("Connection", "close");
        } else {
            this.setDescription("redirect to url '" + url + "'");
            response.sendRedirect(url);
        }
    }
}
