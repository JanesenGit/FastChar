package com.fastchar.servlet;

import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;

public class FastFilterChain {

    public static FastFilterChain newInstance(Object target) {
        return FastChar.getOverrides().newInstance(FastFilterChain.class, target);
    }

    private final Object target;

    public FastFilterChain(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public void doFilter(FastHttpServletRequest request, FastHttpServletResponse response) throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.FilterChain) target).doFilter((javax.servlet.ServletRequest) request.getTarget(), (javax.servlet.ServletResponse) response.getTarget());
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.FilterChain) target).doFilter((jakarta.servlet.ServletRequest) request.getTarget(), (jakarta.servlet.ServletResponse) response.getTarget());
        }

    }
}
