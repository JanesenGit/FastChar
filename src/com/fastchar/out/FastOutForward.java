package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastDispatcher;

public class FastOutForward extends FastOut<FastOutForward> {


    @Override
    public void response(FastAction action) throws Exception {
        this.setDescription("forward to url '" + data + "'");
        new FastDispatcher(action.getRequest(), action.getResponse())
                .setContentUrl(String.valueOf(data))
                .setForwarder(action)
                .invoke();
    }

}
