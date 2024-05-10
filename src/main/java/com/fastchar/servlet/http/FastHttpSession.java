package com.fastchar.servlet.http;

import com.fastchar.core.FastChar;
import com.fastchar.servlet.FastServletContext;
import com.fastchar.servlet.FastServletHelper;

import java.util.Enumeration;
import java.util.Iterator;

public class FastHttpSession {

    public static FastHttpSession newInstance(Object target) {
        return FastChar.getOverrides().newInstance(FastHttpSession.class, target);
    }

    private final Object target;

    public FastHttpSession(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public long getCreationTime() {
        if (target instanceof FastHttpShareSession) {
            return ((FastHttpShareSession) target).getCreationTime();
        }

        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpSession) target).getCreationTime();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpSession) target).getCreationTime();
        }
        return 0;
    }

    public String getId() {
        if (target instanceof FastHttpShareSession) {
            return ((FastHttpShareSession) target).getId();
        }

        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpSession) target).getId();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpSession) target).getId();
        }
        return null;
    }

    public long getLastAccessedTime() {
        if (target instanceof FastHttpShareSession) {
            return ((FastHttpShareSession) target).getLastAccessedTime();
        }

        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpSession) target).getLastAccessedTime();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpSession) target).getLastAccessedTime();
        }
        return 0;
    }

    public FastServletContext getServletContext() {
        if (target instanceof FastHttpShareSession) {
            return ((FastHttpShareSession) target).getServletContext();
        }

        if (FastServletHelper.isJavaxServlet()) {
            return new FastServletContext(((javax.servlet.http.HttpSession) target).getServletContext());
        }

        if (FastServletHelper.isJakartaServlet()) {
            return new FastServletContext(((jakarta.servlet.http.HttpSession) target).getServletContext());
        }
        return null;
    }

    public void setMaxInactiveInterval(int interval) {
        if (target instanceof FastHttpShareSession) {
            FastHttpShareSession httpShareSession = (FastHttpShareSession) target;
            httpShareSession.setMaxInactiveInterval(interval);
            httpShareSession.store();
            return;
        }

        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpSession) target).setMaxInactiveInterval(interval);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpSession) target).setMaxInactiveInterval(interval);
        }
    }

    public int getMaxInactiveInterval() {
        if (target instanceof FastHttpShareSession) {
            return ((FastHttpShareSession) target).getMaxInactiveInterval();
        }

        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpSession) target).getMaxInactiveInterval();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpSession) target).getMaxInactiveInterval();
        }
        return 0;
    }

    public Object getAttribute(String name) {

        if (target instanceof FastHttpShareSession) {
            return ((FastHttpShareSession) target).getAttribute().get(name);
        }

        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpSession) target).getAttribute(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpSession) target).getAttribute(name);
        }
        return null;
    }

    public Enumeration<String> getAttributeNames() {
        if (target instanceof FastHttpShareSession) {
            final Iterator<String> iterator = ((FastHttpShareSession) target).getAttribute().keySet().iterator();
            return new Enumeration<String>() {
                @Override
                public boolean hasMoreElements() {
                    return iterator.hasNext();
                }

                @Override
                public String nextElement() {
                    return iterator.next();
                }
            };
        }


        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpSession) target).getAttributeNames();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpSession) target).getAttributeNames();
        }
        return null;
    }

    public void setAttribute(String name, Object value) {
        if (target instanceof FastHttpShareSession) {
            FastHttpShareSession httpShareSession = (FastHttpShareSession) target;
            httpShareSession.getAttribute().put(name, value);
            httpShareSession.store();
            return;
        }

        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpSession) target).setAttribute(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpSession) target).setAttribute(name, value);
        }
    }

    public void removeAttribute(String name) {
        if (target instanceof FastHttpShareSession) {
            FastHttpShareSession httpShareSession = (FastHttpShareSession) target;
            httpShareSession.getAttribute().remove(name);
            httpShareSession.store();
            return;
        }
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpSession) target).removeAttribute(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpSession) target).removeAttribute(name);
        }
    }

    public void invalidate() {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpSession) target).invalidate();
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpSession) target).invalidate();
        }
    }

    public boolean isNew() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpSession) target).isNew();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpSession) target).isNew();
        }
        return false;
    }
}
