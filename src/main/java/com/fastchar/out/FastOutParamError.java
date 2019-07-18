package com.fastchar.out;

import com.fastchar.core.FastAction;

/**
 * 响应输出参数异常
 */
public class FastOutParamError extends FastOut<FastOutParamError> {
    private String message;
    @Override
    public void response(FastAction action) throws Exception {
        this.setDescription(message);
        action.responseText(400, message);
    }

    public String getMessage() {
        return message;
    }

    public FastOutParamError setMessage(String message) {
        this.message = message;
        return this;
    }
}
