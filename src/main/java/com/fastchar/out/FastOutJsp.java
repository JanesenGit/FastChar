package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;

/**
 * 响应输出jsp
 */
public class FastOutJsp extends FastOut<FastOutJsp> {

    @Override
    public void response(FastAction action) throws Exception {
        setDescription("forward to jsp '" + data + "' ");
        action.getRequest().getRequestDispatcher(String.valueOf(data))
                .forward(action.getRequest(), action.getResponse());

    }
}
