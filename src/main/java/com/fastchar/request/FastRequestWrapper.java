package com.fastchar.request;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * @author 沈建（Janesen）
 * @date 2021/8/24 15:40
 */
public class FastRequestWrapper extends HttpServletRequestWrapper {

    private FastInputStreamWrapper inputStreamWrapper;

    public FastRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (inputStreamWrapper == null) {
            inputStreamWrapper = new FastInputStreamWrapper();
        }
        inputStreamWrapper.setServletInputStream(this, super.getInputStream());
        return inputStreamWrapper;
    }
}
