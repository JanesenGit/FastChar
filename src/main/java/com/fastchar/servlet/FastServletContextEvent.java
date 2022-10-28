package com.fastchar.servlet;

import com.fastchar.core.FastChar;


public class FastServletContextEvent {
    public static FastServletContextEvent newInstance(Object target) {
        return FastChar.getOverrides().newInstance(FastServletContextEvent.class, target);
    }

    private final Object target;

    public FastServletContextEvent(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }


    public FastServletContext getServletContext() {
        if (FastServletHelper.isJavaxServlet()) {
            return FastServletContext.newInstance(((javax.servlet.ServletContextEvent) target).getServletContext());

        }
        if (FastServletHelper.isJakartaServlet()) {
            return FastServletContext.newInstance(((jakarta.servlet.ServletContextEvent) target).getServletContext());
        }
        return null;
    }
}
