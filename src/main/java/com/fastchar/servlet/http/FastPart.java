package com.fastchar.servlet.http;

import com.fastchar.core.FastChar;
import com.fastchar.servlet.FastServletHelper;
import com.fastchar.utils.FastClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class FastPart {
    public static FastPart newInstance(Object target) {
        return FastChar.getOverrides().newInstance(FastPart.class, target);
    }

    private final Object target;

    public FastPart(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public InputStream getInputStream() throws IOException {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Part) target).getInputStream();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Part) target).getInputStream();
        }
        return null;
    }

    public String getContentType() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Part) target).getContentType();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Part) target).getContentType();
        }
        return null;
    }

    public String getName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Part) target).getName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Part) target).getName();
        }
        return null;
    }


    public long getSize() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Part) target).getSize();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Part) target).getSize();
        }
        return 0;
    }

    public void write(String fileName) throws IOException {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Part) target).write(fileName);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Part) target).write(fileName);
        }
    }

    public void delete() throws IOException {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.Part) target).delete();
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.Part) target).delete();
        }
    }

    public String getHeader(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Part) target).getHeader(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Part) target).getHeader(name);
        }
        return null;
    }

    public Collection<String> getHeaders(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Part) target).getHeaders(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Part) target).getHeaders(name);
        }
        return null;
    }

    public Collection<String> getHeaderNames() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.Part) target).getHeaderNames();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.Part) target).getHeaderNames();
        }
        return null;
    }

    public String getSubmittedFileName() {
        Object getSubmittedFileName = FastClassUtils.safeInvokeMethod(target, "getSubmittedFileName");
        if (getSubmittedFileName != null) {
            return getSubmittedFileName.toString();
        }
        return null;
    }

}
