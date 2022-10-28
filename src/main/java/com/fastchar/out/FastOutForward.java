package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastDispatcher;
import com.fastchar.core.FastRequestLog;
import com.fastchar.utils.FastStringUtils;

/**
 * 转发请求
 */
public class FastOutForward extends FastOut<FastOutForward> {


    @Override
    public void response(FastAction action) throws Exception {
        this.setDescription("forward to url '" + data + "'");
        String url = String.valueOf(data);
        if (FastStringUtils.splitByWholeSeparator(url,"?")[0].lastIndexOf(".") > 0) {
            action.getRequest().getRequestDispatcher(url)
                    .forward(action.getRequest(), action.getResponse());
            FastRequestLog.log(action);
            return;
        }
        new FastDispatcher(action.getRequest(), action.getResponse())
                .setContentUrl(url)
                .setForwarder(action)
                .invoke();
    }

}
