package com.fastchar.servlet.http;


import com.fastchar.core.FastChar;
import com.fastchar.servlet.FastServletHelper;

public class FastRequestDispatcher {
    public static FastRequestDispatcher newInstance(Object target) {
        return FastChar.getOverrides().newInstance(FastRequestDispatcher.class, target);
    }

    private final Object target;

    public FastRequestDispatcher(Object target) {
        this.target = target;
    }

    public void forward(FastHttpServletRequest request, FastHttpServletResponse response) throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.RequestDispatcher) target).forward((javax.servlet.ServletRequest) request.getTarget(), (javax.servlet.ServletResponse) response.getTarget());
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.RequestDispatcher) target).forward((jakarta.servlet.ServletRequest) request.getTarget(), (jakarta.servlet.ServletResponse) response.getTarget());
        }
    }

    public void include(FastHttpServletRequest request, FastHttpServletResponse response) throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.RequestDispatcher) target).include((javax.servlet.ServletRequest) request.getTarget(), (javax.servlet.ServletResponse) response.getTarget());
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.RequestDispatcher) target).include((jakarta.servlet.ServletRequest) request.getTarget(), (jakarta.servlet.ServletResponse) response.getTarget());
        }
    }
}

