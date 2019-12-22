package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.utils.FastNumberUtils;

import javax.servlet.http.HttpServletResponse;

public class FastOutStatus extends FastOut<FastOutStatus> {
    @Override
    public void response(FastAction action) throws Exception {
        HttpServletResponse response = action.getResponse();
        response.setStatus(FastNumberUtils.formatToInt(data));
    }
}
