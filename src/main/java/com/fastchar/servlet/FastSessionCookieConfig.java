package com.fastchar.servlet;

public  class FastSessionCookieConfig  {
    private final Object target;

    public FastSessionCookieConfig(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public void setName(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.SessionCookieConfig) target).setName(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.SessionCookieConfig) target).setName(name);
        }
    }

    public String getName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.SessionCookieConfig) target).getName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.SessionCookieConfig) target).getName();
        }
        return null;
    }

    public void setDomain(String domain) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.SessionCookieConfig) target).setDomain(domain);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.SessionCookieConfig) target).setDomain(domain);
        }
    }

    public String getDomain() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.SessionCookieConfig) target).getDomain();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.SessionCookieConfig) target).getDomain();
        }
        return null;
    }

    public void setPath(String path) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.SessionCookieConfig) target).setPath(path);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.SessionCookieConfig) target).setPath(path);
        }
    }

    public String getPath() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.SessionCookieConfig) target).getPath();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.SessionCookieConfig) target).getPath();
        }
        return null;
    }

    public void setComment(String comment) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.SessionCookieConfig) target).setComment(comment);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.SessionCookieConfig) target).setComment(comment);
        }
    }

    public String getComment() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.SessionCookieConfig) target).getComment();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.SessionCookieConfig) target).getComment();
        }
        return null;
    }

    public void setHttpOnly(boolean httpOnly) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.SessionCookieConfig) target).setHttpOnly(httpOnly);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.SessionCookieConfig) target).setHttpOnly(httpOnly);
        }
    }

    public boolean isHttpOnly() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.SessionCookieConfig) target).isHttpOnly();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.SessionCookieConfig) target).isHttpOnly();
        }
        return false;
    }

    public void setSecure(boolean secure) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.SessionCookieConfig) target).setSecure(secure);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.SessionCookieConfig) target).setSecure(secure);
        }
    }

    public boolean isSecure() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.SessionCookieConfig) target).isSecure();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.SessionCookieConfig) target).isSecure();
        }
        return false;
    }

    public void setMaxAge(int maxAge) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.SessionCookieConfig) target).setMaxAge(maxAge);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.SessionCookieConfig) target).setMaxAge(maxAge);
        }
    }

    public int getMaxAge() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.SessionCookieConfig) target).getMaxAge();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.SessionCookieConfig) target).getMaxAge();
        }
        return 0;
    }
}
