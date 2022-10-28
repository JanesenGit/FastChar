package com.fastchar.servlet;


import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastRequestDispatcher;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class FastServletContext {

    public static FastServletContext newInstance(Object target) {
        return FastChar.getOverrides().newInstance(FastServletContext.class, target);
    }


    private final Object target;

    public FastServletContext(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public String getContextPath() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getContextPath();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getContextPath();
        }
        return null;
    }

    public FastServletContext getContext(String uripath) {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastServletContext(((javax.servlet.ServletContext) target).getContext(uripath));
        }

        if (FastServletHelper.isJakartaServlet()) {
            return new FastServletContext(((jakarta.servlet.ServletContext) target).getContext(uripath));
        }
        return null;
    }

    public int getMajorVersion() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getMajorVersion();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getMajorVersion();
        }
        return 0;
    }

    public int getMinorVersion() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getMinorVersion();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getMinorVersion();
        }
        return 0;
    }

    public int getEffectiveMajorVersion() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getEffectiveMajorVersion();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getEffectiveMajorVersion();
        }
        return 0;
    }

    public int getEffectiveMinorVersion() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getEffectiveMinorVersion();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getEffectiveMinorVersion();
        }
        return 0;
    }

    public String getMimeType(String file) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getMimeType(file);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getMimeType(file);
        }
        return null;
    }

    public Set<String> getResourcePaths(String path) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getResourcePaths(path);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getResourcePaths(path);
        }
        return null;
    }

    public URL getResource(String path) throws MalformedURLException {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getResource(path);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getResource(path);
        }
        return null;
    }

    public InputStream getResourceAsStream(String path) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getResourceAsStream(path);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getResourceAsStream(path);
        }
        return null;
    }

    public FastRequestDispatcher getRequestDispatcher(String path) {
        if (FastServletHelper.isJavaxServlet()) {
            return FastRequestDispatcher.newInstance(((javax.servlet.ServletContext) target).getRequestDispatcher(path));
        }

        if (FastServletHelper.isJakartaServlet()) {
            return FastRequestDispatcher.newInstance(((jakarta.servlet.ServletContext) target).getRequestDispatcher(path));
        }
        return null;
    }

    public FastRequestDispatcher getNamedDispatcher(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return FastRequestDispatcher.newInstance(((javax.servlet.ServletContext) target).getNamedDispatcher(name));
        }

        if (FastServletHelper.isJakartaServlet()) {
            return FastRequestDispatcher.newInstance(((jakarta.servlet.ServletContext) target).getNamedDispatcher(name));
        }
        return null;
    }

    public String getRealPath(String path) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getRealPath(path);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getRealPath(path);
        }
        return null;
    }

    public String getServerInfo() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getServerInfo();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getServerInfo();
        }
        return null;
    }

    public String getInitParameter(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getInitParameter(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getInitParameter(name);
        }
        return null;
    }

    public Enumeration<String> getInitParameterNames() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getInitParameterNames();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getInitParameterNames();
        }
        return null;
    }

    public boolean setInitParameter(String name, String value) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).setInitParameter(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).setInitParameter(name, value);
        }
        return false;
    }

    public Object getAttribute(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getAttribute(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getAttribute(name);
        }
        return null;
    }

    public Enumeration<String> getAttributeNames() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getAttributeNames();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getAttributeNames();
        }
        return null;
    }

    public void setAttribute(String name, Object object) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.ServletContext) target).setAttribute(name, object);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.ServletContext) target).setAttribute(name, object);
        }
    }

    public void removeAttribute(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.ServletContext) target).removeAttribute(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.ServletContext) target).removeAttribute(name);
        }
    }

    public String getServletContextName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getServletContextName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getServletContextName();
        }
        return null;
    }


    public void log(String msg) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.ServletContext) target).log(msg);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.ServletContext) target).log(msg);
        }
    }

    public void log(String message, Throwable throwable) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.ServletContext) target).log(message, throwable);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.ServletContext) target).log(message, throwable);
        }
    }

    public FastServletRegistration addServlet(String servletName, String className) {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastServletRegistration(((javax.servlet.ServletContext) target).addServlet(servletName, className));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastServletRegistration(((jakarta.servlet.ServletContext) target).addServlet(servletName, className));
        }
        return null;
    }

    public FastServletRegistration addServlet(String servletName, Object servlet) {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastServletRegistration(((javax.servlet.ServletContext) target)
                    .addServlet(servletName, (javax.servlet.Servlet) servlet));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastServletRegistration(((jakarta.servlet.ServletContext) target)
                    .addServlet(servletName, (jakarta.servlet.Servlet) servlet));
        }
        return null;
    }

    public FastServletRegistration addServlet(String servletName, Class<?> servletClass) {

        if (FastServletHelper.isJavaxServlet()) {
            return new FastServletRegistration(((javax.servlet.ServletContext) target).addServlet(servletName,
                    (Class<? extends javax.servlet.Servlet>) servletClass));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastServletRegistration(((jakarta.servlet.ServletContext) target).addServlet(servletName,
                    (Class<? extends jakarta.servlet.Servlet>) servletClass));
        }
        return null;
    }


    public FastServletRegistration getServletRegistration(String servletName) {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastServletRegistration(((javax.servlet.ServletContext) target).getServletRegistration(servletName));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastServletRegistration(((jakarta.servlet.ServletContext) target).getServletRegistration(servletName));
        }
        return null;
    }

    public Map<String, ? extends FastServletRegistration> getServletRegistrations() {
        if (FastServletHelper.isJavaxServlet()) {
            Map<String, FastServletRegistration> map = new HashMap<>(16);
            Map<String, ? extends javax.servlet.ServletRegistration> servletRegistrations = ((javax.servlet.ServletContext) target).getServletRegistrations();

            for (Map.Entry<String, ? extends ServletRegistration> stringEntry : servletRegistrations.entrySet()) {
                map.put(stringEntry.getKey(), new FastServletRegistration(stringEntry.getValue()));
            }
            return map;
        }
        if (FastServletHelper.isJakartaServlet()) {
            Map<String, FastServletRegistration> map = new HashMap<>(16);
            Map<String, ? extends jakarta.servlet.ServletRegistration> servletRegistrations = ((jakarta.servlet.ServletContext) target).getServletRegistrations();
            for (Map.Entry<String, ? extends jakarta.servlet.ServletRegistration> stringEntry : servletRegistrations.entrySet()) {
                map.put(stringEntry.getKey(), new FastServletRegistration(stringEntry.getValue()));
            }
            return map;
        }
        return null;
    }

    public FastFilterRegistration addFilter(String filterName, String className) {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastFilterRegistration(((javax.servlet.ServletContext) target)
                    .addFilter(filterName, className));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastFilterRegistration(((jakarta.servlet.ServletContext) target)
                    .addFilter(filterName, className));
        }
        return null;
    }

    public FastFilterRegistration addFilter(String filterName, Object filter) {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastFilterRegistration(((javax.servlet.ServletContext) target).addFilter(filterName,
                    (Class<? extends javax.servlet.Filter>) filter));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastFilterRegistration(((jakarta.servlet.ServletContext) target).addFilter(filterName,
                    (Class<? extends jakarta.servlet.Filter>) filter));
        }
        return null;
    }


    public FastFilterRegistration addFilter(String filterName, Class<?> filterClass) {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastFilterRegistration(((javax.servlet.ServletContext) target).addFilter(filterName,
                    (Class<? extends javax.servlet.Filter>) filterClass));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastFilterRegistration(((jakarta.servlet.ServletContext) target).addFilter(filterName,
                    (Class<? extends jakarta.servlet.Filter>) filterClass));
        }
        return null;
    }


    public FastFilterRegistration getFilterRegistration(String filterName) {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastFilterRegistration(((javax.servlet.ServletContext) target).getFilterRegistration(filterName));
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastFilterRegistration(((jakarta.servlet.ServletContext) target).getFilterRegistration(filterName));
        }
        return null;
    }

    public Map<String, ? extends FastFilterRegistration> getFilterRegistrations() {
        if (FastServletHelper.isJavaxServlet()) {
            Map<String, FastFilterRegistration> map = new HashMap<>(16);
            Map<String, ? extends javax.servlet.FilterRegistration> filterRegistrations = ((javax.servlet.ServletContext) target).getFilterRegistrations();

            for (Map.Entry<String, ? extends FilterRegistration> stringEntry : filterRegistrations.entrySet()) {
                map.put(stringEntry.getKey(), new FastFilterRegistration(stringEntry.getValue()));
            }
            return map;
        }
        if (FastServletHelper.isJakartaServlet()) {
            Map<String, FastFilterRegistration> map = new HashMap<>(16);
            Map<String, ? extends jakarta.servlet.FilterRegistration> filterRegistrations = ((jakarta.servlet.ServletContext) target).getFilterRegistrations();
            for (Map.Entry<String, ? extends jakarta.servlet.FilterRegistration> stringEntry : filterRegistrations.entrySet()) {
                map.put(stringEntry.getKey(), new FastFilterRegistration(stringEntry.getValue()));
            }
            return map;
        }
        return null;
    }

    public FastSessionCookieConfig getSessionCookieConfig() {
        if (FastServletHelper.isJavaxServlet()) {
            return new FastSessionCookieConfig(((javax.servlet.ServletContext) target).getSessionCookieConfig());
        }
        if (FastServletHelper.isJakartaServlet()) {
            return new FastSessionCookieConfig(((jakarta.servlet.ServletContext) target).getSessionCookieConfig());
        }
        return null;
    }

    public void setSessionTrackingModes(Set<FastSessionTrackingMode> sessionTrackingModes) {
        if (FastServletHelper.isJavaxServlet()) {
            Set<javax.servlet.SessionTrackingMode> toSessionTrackingModes = new HashSet<>();
            for (FastSessionTrackingMode sessionTrackingMode : sessionTrackingModes) {
                toSessionTrackingModes.add(javax.servlet.SessionTrackingMode.valueOf(sessionTrackingMode.name()));
            }
            ((javax.servlet.ServletContext) target).setSessionTrackingModes(toSessionTrackingModes);
        }
        if (FastServletHelper.isJakartaServlet()) {
            Set<jakarta.servlet.SessionTrackingMode> toSessionTrackingModes = new HashSet<>();
            for (FastSessionTrackingMode sessionTrackingMode : sessionTrackingModes) {
                toSessionTrackingModes.add(jakarta.servlet.SessionTrackingMode.valueOf(sessionTrackingMode.name()));
            }
            ((jakarta.servlet.ServletContext) target).setSessionTrackingModes(toSessionTrackingModes);
        }
    }

    public Set<FastSessionTrackingMode> getDefaultSessionTrackingModes() {
        Set<FastSessionTrackingMode> toSessionTrackingModes = new HashSet<>();

        if (FastServletHelper.isJavaxServlet()) {
            Set<javax.servlet.SessionTrackingMode> sessionTrackingModes = ((javax.servlet.ServletContext) target).getDefaultSessionTrackingModes();
            for (javax.servlet.SessionTrackingMode sessionTrackingMode : sessionTrackingModes) {
                toSessionTrackingModes.add(FastSessionTrackingMode.valueOf(sessionTrackingMode.name()));
            }
        }
        if (FastServletHelper.isJakartaServlet()) {
            Set<jakarta.servlet.SessionTrackingMode> sessionTrackingModes = ((jakarta.servlet.ServletContext) target).getDefaultSessionTrackingModes();
            for (jakarta.servlet.SessionTrackingMode sessionTrackingMode : sessionTrackingModes) {
                toSessionTrackingModes.add(FastSessionTrackingMode.valueOf(sessionTrackingMode.name()));
            }
        }
        return toSessionTrackingModes;
    }

    public Set<FastSessionTrackingMode> getEffectiveSessionTrackingModes() {
        Set<FastSessionTrackingMode> toSessionTrackingModes = new HashSet<>();

        if (FastServletHelper.isJavaxServlet()) {
            Set<javax.servlet.SessionTrackingMode> sessionTrackingModes = ((javax.servlet.ServletContext) target).getEffectiveSessionTrackingModes();
            for (javax.servlet.SessionTrackingMode sessionTrackingMode : sessionTrackingModes) {
                toSessionTrackingModes.add(FastSessionTrackingMode.valueOf(sessionTrackingMode.name()));
            }
        }
        if (FastServletHelper.isJakartaServlet()) {
            Set<jakarta.servlet.SessionTrackingMode> sessionTrackingModes = ((jakarta.servlet.ServletContext) target).getEffectiveSessionTrackingModes();
            for (jakarta.servlet.SessionTrackingMode sessionTrackingMode : sessionTrackingModes) {
                toSessionTrackingModes.add(FastSessionTrackingMode.valueOf(sessionTrackingMode.name()));
            }
        }
        return toSessionTrackingModes;
    }

    public void addListener(String className) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.ServletContext) target).addListener(className);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.ServletContext) target).addListener(className);
        }
    }

    public <T extends EventListener> void addListener(T t) {
        if (FastServletHelper.isJavaxServlet()) {
            if (t instanceof FastServletContextListener) {
                t = ((FastServletContextListener) t).createTarget();
            }
            ((javax.servlet.ServletContext) target).addListener(t);
        }
        if (FastServletHelper.isJakartaServlet()) {
            if (t instanceof FastServletContextListener) {
                t = ((FastServletContextListener) t).createTarget();
            }
            ((jakarta.servlet.ServletContext) target).addListener(t);
        }
    }

    public void addListener(Class<? extends EventListener> listenerClass) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.ServletContext) target).addListener(listenerClass);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.ServletContext) target).addListener(listenerClass);
        }
    }

    public <T extends EventListener> T createListener(Class<T> clazz) throws Exception {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).createListener(clazz);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).createListener(clazz);
        }
        return null;
    }

    public ClassLoader getClassLoader() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletContext) target).getClassLoader();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletContext) target).getClassLoader();
        }
        return null;
    }

    public void declareRoles(String... roleNames) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.ServletContext) target).declareRoles(roleNames);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.ServletContext) target).declareRoles(roleNames);
        }
    }
}
