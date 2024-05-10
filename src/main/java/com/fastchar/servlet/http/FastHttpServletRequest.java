package com.fastchar.servlet.http;

import com.fastchar.core.FastChar;
import com.fastchar.enums.FastServerType;
import com.fastchar.servlet.FastDispatcherType;
import com.fastchar.servlet.FastHttpHeaders;
import com.fastchar.servlet.FastServletContext;
import com.fastchar.servlet.FastServletHelper;
import com.fastchar.servlet.http.tomcat.FastHttpServletRequestMultipartConfig;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.*;

public class FastHttpServletRequest {

    public static FastHttpServletRequest newInstance(Object target) {
        return FastChar.getOverrides().newInstance(FastHttpServletRequest.class, target);
    }

    private final Object target;

    public FastHttpServletRequest(Object target) {
        this.target = target;
        if (isMultipart()) {
            //如果是multipart，必须先配置，否则无法正常获取到参数！
            multipartConfig();
        }
    }

    public Object getTarget() {
        return target;
    }


    public boolean isMultipart() {
        if (!getMethod().equalsIgnoreCase("post")) {
            return false;
        }
        String contentType = getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }


    public String getRequestURI() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getRequestURI();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getRequestURI();
        }
        return null;
    }

    public String getMethod() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getMethod();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getMethod();
        }
        return null;
    }

    public String getPathInfo() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getPathInfo();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getPathInfo();
        }
        return null;
    }

    public String getPathTranslated() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getPathTranslated();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getPathTranslated();
        }
        return null;
    }

    public String getContextPath() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getContextPath();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getContextPath();
        }
        return null;
    }

    public String getQueryString() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getQueryString();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getQueryString();
        }
        return null;
    }

    public String getRemoteUser() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getRemoteUser();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getRemoteUser();
        }
        return null;
    }

    public boolean isUserInRole(String role) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).isUserInRole(role);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).isUserInRole(role);
        }
        return false;
    }

    public Principal getUserPrincipal() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getUserPrincipal();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getUserPrincipal();
        }
        return null;
    }

    public String getRequestedSessionId() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getRequestedSessionId();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getRequestedSessionId();
        }
        return null;
    }


    public InputStream getInputStream() throws IOException {
        //此处需要动态调用并执行方法，因为classloader会提前编译检测调用的类
        Object getInputStream = FastClassUtils.safeInvokeMethod(target, "getInputStream");
        if (getInputStream != null) {
            return (InputStream) getInputStream;
        }
        return null;
    }

    public String getHeader(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getHeader(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getHeader(name);
        }
        return null;
    }

    public Enumeration<String> getHeaders(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getHeaders(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getHeaders(name);
        }
        return null;
    }

    public Enumeration<String> getHeaderNames() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getHeaderNames();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getHeaderNames();
        }
        return null;
    }

    public String getAuthType() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getAuthType();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getAuthType();
        }
        return null;
    }

    public FastCookie[] getCookies() {
        if (FastServletHelper.isJavaxServlet()) {
            List<FastCookie> cookieList = new ArrayList<>();
            Object[] cookies = ((javax.servlet.http.HttpServletRequest) target).getCookies();
            for (Object cookie : cookies) {
                cookieList.add(FastCookie.newInstance(cookie));
            }
            return cookieList.toArray(new FastCookie[]{});
        }

        if (FastServletHelper.isJakartaServlet()) {
            List<FastCookie> cookieList = new ArrayList<>();
            Object[] cookies = ((jakarta.servlet.http.HttpServletRequest) target).getCookies();
            for (Object cookie : cookies) {
                cookieList.add(FastCookie.newInstance(cookie));
            }
            return cookieList.toArray(new FastCookie[]{});
        }
        return new FastCookie[0];
    }

    public long getDateHeader(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getDateHeader(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getDateHeader(name);
        }
        return 0;
    }

    public int getIntHeader(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getIntHeader(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getIntHeader(name);
        }
        return 0;
    }

    public StringBuffer getRequestURL() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getRequestURL();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getRequestURL();
        }
        return null;
    }

    public String getServletPath() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getServletPath();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getServletPath();
        }
        return null;
    }


    private FastHttpSession getSessionFromHeader() {
        String sessionId = getHeader(FastHttpHeaders.SESSION_ID);
        if (FastStringUtils.isNotEmpty(sessionId)) {
            return FastHttpSession.newInstance(FastHttpShareSessionFactory.getSession(sessionId));
        }
        return null;
    }

    public FastHttpSession getSession(boolean create) {

        FastHttpSession sessionFromHeader = getSessionFromHeader();
        if (sessionFromHeader != null) {
            return sessionFromHeader;
        }

        if (FastServletHelper.isJavaxServlet()) {
            return FastHttpSession.newInstance(((javax.servlet.http.HttpServletRequest) target).getSession(create));
        }

        if (FastServletHelper.isJakartaServlet()) {
            return FastHttpSession.newInstance(((jakarta.servlet.http.HttpServletRequest) target).getSession(create));
        }
        return null;
    }

    public FastHttpSession getSession() {
        FastHttpSession sessionFromHeader = getSessionFromHeader();
        if (sessionFromHeader != null) {
            return sessionFromHeader;
        }

        if (FastServletHelper.isJavaxServlet()) {
            return FastHttpSession.newInstance(((javax.servlet.http.HttpServletRequest) target).getSession());
        }

        if (FastServletHelper.isJakartaServlet()) {
            return FastHttpSession.newInstance(((jakarta.servlet.http.HttpServletRequest) target).getSession());
        }
        return null;
    }

    public String changeSessionId() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).changeSessionId();
        }

        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).changeSessionId();
        }
        return null;
    }

    public boolean isRequestedSessionIdValid() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).isRequestedSessionIdValid();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).isRequestedSessionIdValid();
        }
        return false;
    }

    public boolean isRequestedSessionIdFromCookie() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).isRequestedSessionIdFromCookie();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).isRequestedSessionIdFromCookie();
        }
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).isRequestedSessionIdFromURL();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).isRequestedSessionIdFromURL();
        }
        return false;
    }


    public boolean authenticate(Object response) throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).authenticate((javax.servlet.http.HttpServletResponse) response);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).authenticate((jakarta.servlet.http.HttpServletResponse) response);
        }
        return false;
    }

    public void login(String username, String password) throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletRequest) target).login(username, password);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletRequest) target).login(username, password);
        }
    }

    public void logout() throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletRequest) target).logout();
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletRequest) target).logout();
        }
    }

    public String getContentType() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getContentType();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getContentType();
        }
        return null;
    }


    public String getParameter(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getParameter(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getParameter(name);
        }
        return null;
    }

    public Enumeration<String> getParameterNames() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getParameterNames();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getParameterNames();
        }
        return null;
    }

    public String[] getParameterValues(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getParameterValues(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getParameterValues(name);
        }
        return new String[0];
    }


    public Object getAttribute(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getAttribute(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getAttribute(name);
        }
        return null;
    }

    public String getCharacterEncoding() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getCharacterEncoding();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getCharacterEncoding();
        }
        return null;
    }

    public void setCharacterEncoding(String env) throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletRequest) target).setCharacterEncoding(env);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletRequest) target).setCharacterEncoding(env);
        }
    }

    public int getContentLength() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getContentLength();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getContentLength();
        }
        return 0;
    }

    public long getContentLengthLong() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getContentLength();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getContentLengthLong();
        }
        return 0;
    }


    public String getRemoteAddr() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getRemoteAddr();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getRemoteAddr();
        }
        return null;
    }

    public String getRemoteHost() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getRemoteHost();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getRemoteHost();
        }
        return null;
    }

    public void setAttribute(String name, Object value) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletRequest) target).setAttribute(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletRequest) target).setAttribute(name, value);
        }
    }

    public void removeAttribute(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletRequest) target).removeAttribute(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletRequest) target).removeAttribute(name);
        }
    }

    public Locale getLocale() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getLocale();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getLocale();
        }
        return null;
    }

    public Enumeration<Locale> getLocales() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getLocales();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getLocales();
        }
        return null;
    }

    public boolean isSecure() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).isSecure();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).isSecure();
        }
        return false;
    }

    public FastRequestDispatcher getRequestDispatcher(String path) {
        if (FastServletHelper.isJavaxServlet()) {
            return FastRequestDispatcher.newInstance(((javax.servlet.http.HttpServletRequest) target).getRequestDispatcher(path));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return FastRequestDispatcher.newInstance(((jakarta.servlet.http.HttpServletRequest) target).getRequestDispatcher(path));
        }

        return null;
    }

    public int getRemotePort() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getRemotePort();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getRemotePort();
        }
        return 0;
    }

    public String getLocalName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getLocalName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getLocalName();
        }
        return null;
    }

    public String getLocalAddr() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getLocalAddr();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getLocalAddr();
        }
        return null;
    }

    public int getLocalPort() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getLocalPort();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getLocalPort();
        }
        return 0;
    }

    public FastServletContext getServletContext() {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastServletContext(((javax.servlet.http.HttpServletRequest) target).getServletContext());
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastServletContext(((jakarta.servlet.http.HttpServletRequest) target).getServletContext());
        }
        return null;
    }

    public FastDispatcherType getDispatcherType() {
        if (FastServletHelper.isJavaxServlet()) {
            return FastDispatcherType.valueOf(((javax.servlet.http.HttpServletRequest) target).getDispatcherType().name());
        }
        if (FastServletHelper.isJakartaServlet()) {
            return FastDispatcherType.valueOf(((jakarta.servlet.http.HttpServletRequest) target).getDispatcherType().name());
        }
        return null;
    }

    public Collection<FastPart> getParts() throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            List<FastPart> partList = new ArrayList<>();
            Collection<javax.servlet.http.Part> parts = ((javax.servlet.http.HttpServletRequest) target).getParts();
            for (Object part : parts) {
                partList.add(FastPart.newInstance(part));
            }
            return partList;
        }

        if (FastServletHelper.isJakartaServlet()) {
            List<FastPart> partList = new ArrayList<>();
            Collection<jakarta.servlet.http.Part> parts = ((jakarta.servlet.http.HttpServletRequest) target).getParts();
            for (Object part : parts) {
                partList.add(FastPart.newInstance(part));
            }
            return partList;
        }
        return null;
    }

    public FastPart getPart(String name) throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            return FastPart.newInstance(((javax.servlet.http.HttpServletRequest) target).getPart(name));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return FastPart.newInstance(((jakarta.servlet.http.HttpServletRequest) target).getPart(name));
        }
        return null;
    }

    public Map<String, String[]> getParameterMap() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getParameterMap();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getParameterMap();
        }
        return null;
    }

    public String getProtocol() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getProtocol();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getProtocol();
        }
        return null;
    }

    public String getScheme() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getScheme();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getScheme();
        }
        return null;
    }

    public String getServerName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getServerName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getServerName();
        }
        return null;
    }

    public int getServerPort() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getServerPort();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getServerPort();
        }
        return 0;
    }

    public BufferedReader getReader() throws IOException {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getReader();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getReader();
        }
        return null;
    }


    public Enumeration<String> getAttributeNames() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletRequest) target).getAttributeNames();
        }

        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletRequest) target).getAttributeNames();
        }
        return null;
    }

    public void multipartConfig() {
        if (FastServletHelper.isJavaxServlet()) {
            Object request = target;
            if (target instanceof javax.servlet.http.HttpServletRequestWrapper) {
                javax.servlet.http.HttpServletRequestWrapper requestWrapper = (javax.servlet.http.HttpServletRequestWrapper) request;
                if (requestWrapper.getRequest() instanceof javax.servlet.http.HttpServletRequest) {
                    request = requestWrapper.getRequest();
                }
            }
            if (FastChar.getConstant().getServerType() == FastServerType.Tomcat) {
                new FastHttpServletRequestMultipartConfig(request).multipartConfig();
            }
        }


        if (FastServletHelper.isJakartaServlet()) {
            Object request = target;
            if (target instanceof jakarta.servlet.http.HttpServletRequestWrapper) {
                jakarta.servlet.http.HttpServletRequestWrapper requestWrapper = (jakarta.servlet.http.HttpServletRequestWrapper) request;
                if (requestWrapper.getRequest() instanceof jakarta.servlet.http.HttpServletRequest) {
                    request = requestWrapper.getRequest();
                }
            }
            if (FastChar.getConstant().getServerType() == FastServerType.Tomcat) {
                new FastHttpServletRequestMultipartConfig(request).multipartConfig();
            }
        }
    }

}

