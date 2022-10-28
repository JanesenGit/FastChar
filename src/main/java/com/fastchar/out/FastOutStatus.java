package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.utils.FastNumberUtils;

/**
 * 响应状态
 */
public class FastOutStatus extends FastOut<FastOutStatus> {
    @Override
    public void response(FastAction action) throws Exception {
        FastHttpServletResponse response = action.getResponse();
        response.setStatus(FastNumberUtils.formatToInt(data));
    }
}
