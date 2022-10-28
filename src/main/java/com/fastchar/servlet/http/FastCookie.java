package com.fastchar.servlet.http;

import com.fastchar.core.FastChar;
import com.fastchar.servlet.FastServletHelper;

public class FastCookie  {
    public static FastCookie newInstance(Object target) {
        return FastChar.getOverrides().newInstance(FastCookie.class, target);
    }

    public static FastCookie newInstance(String name, String value) {
        return FastChar.getOverrides().newInstance(FastCookie.class, name, value);
    }

    private Object target;

    public FastCookie(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public FastCookie(String name, String value) {
        if (FastServletHelper.isJavaxServlet()) {
            target = new javax.servlet.http.Cookie(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            target = new jakarta.servlet.http.Cookie(name, value);
        }
    }

    public void setComment(String purpose) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Cookie) target).setComment(purpose);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Cookie) target).setComment(purpose);
        }
    }

    public String getComment() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Cookie) target).getComment();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Cookie) target).getComment();
        }
        return null;
    }

    public void setDomain(String domain) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Cookie) target).setDomain(domain);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Cookie) target).setDomain(domain);
        }
    }

    public String getDomain() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Cookie) target).getDomain();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Cookie) target).getDomain();
        }
        return null;
    }

    public void setMaxAge(int expiry) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Cookie) target).setMaxAge(expiry);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Cookie) target).setMaxAge(expiry);
        }
    }

    public int getMaxAge() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Cookie) target).getMaxAge();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Cookie) target).getMaxAge();
        }
        return 0;
    }

    public void setPath(String uri) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Cookie) target).setPath(uri);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Cookie) target).setPath(uri);
        }
    }

    public String getPath() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Cookie) target).getPath();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Cookie) target).getPath();
        }
        return null;
    }

    public void setSecure(boolean flag) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Cookie) target).setSecure(flag);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Cookie) target).setSecure(flag);
        }
    }

    public boolean getSecure() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Cookie) target).getSecure();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Cookie) target).getSecure();
        }
        return false;
    }

    public String getName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Cookie) target).getName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Cookie) target).getName();
        }
        return null;
    }

    public void setValue(String newValue) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Cookie) target).setValue(newValue);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Cookie) target).setValue(newValue);
        }
    }

    public String getValue() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Cookie) target).getValue();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Cookie) target).getValue();
        }
        return null;
    }

    public int getVersion() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Cookie) target).getVersion();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Cookie) target).getVersion();
        }
        return 0;
    }

    public void setVersion(int v) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Cookie) target).setVersion(v);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Cookie) target).setVersion(v);
        }
    }


    public void setHttpOnly(boolean isHttpOnly) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Cookie) target).setHttpOnly(isHttpOnly);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Cookie) target).setHttpOnly(isHttpOnly);
        }
    }

    public boolean isHttpOnly() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Cookie) target).isHttpOnly();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Cookie) target).isHttpOnly();
        }
        return false;
    }
}
