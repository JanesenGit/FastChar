package com.fastchar.servlet;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class FastFilterRegistration {

    private final Object target;

    public FastFilterRegistration(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public void addMappingForServletNames(EnumSet<FastDispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
        if (FastServletHelper.isJavaxServlet()) {
            EnumSet<javax.servlet.DispatcherType> toDispatcherTypes = null;
            for (FastDispatcherType dispatcherType : dispatcherTypes) {
                javax.servlet.DispatcherType type = javax.servlet.DispatcherType.valueOf(dispatcherType.name());
                if (toDispatcherTypes == null) {
                    toDispatcherTypes = EnumSet.of(type);
                } else {
                    toDispatcherTypes.add(type);
                }
            }
            ((javax.servlet.FilterRegistration) target).addMappingForServletNames(toDispatcherTypes, isMatchAfter, servletNames);
        }
        if (FastServletHelper.isJakartaServlet()) {
            EnumSet<jakarta.servlet.DispatcherType> toDispatcherTypes = null;
            for (FastDispatcherType dispatcherType : dispatcherTypes) {
                jakarta.servlet.DispatcherType type = jakarta.servlet.DispatcherType.valueOf(dispatcherType.name());
                if (toDispatcherTypes == null) {
                    toDispatcherTypes = EnumSet.of(type);
                } else {
                    toDispatcherTypes.add(type);
                }
            }
            ((jakarta.servlet.FilterRegistration) target).addMappingForServletNames(toDispatcherTypes, isMatchAfter, servletNames);
        }
    }

    public Collection<String> getServletNameMappings() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.FilterRegistration) target).getServletNameMappings();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((javax.servlet.FilterRegistration) target).getServletNameMappings();
        }
        return null;
    }

    public void addMappingForUrlPatterns(EnumSet<FastDispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        if (FastServletHelper.isJavaxServlet()) {
            EnumSet<javax.servlet.DispatcherType> toDispatcherTypes = null;
            for (FastDispatcherType dispatcherType : dispatcherTypes) {
                javax.servlet.DispatcherType type = javax.servlet.DispatcherType.valueOf(dispatcherType.name());
                if (toDispatcherTypes == null) {
                    toDispatcherTypes = EnumSet.of(type);
                } else {
                    toDispatcherTypes.add(type);
                }
            }
            ((javax.servlet.FilterRegistration) target).addMappingForUrlPatterns(toDispatcherTypes, isMatchAfter, urlPatterns);
        }
        if (FastServletHelper.isJakartaServlet()) {
            EnumSet<jakarta.servlet.DispatcherType> toDispatcherTypes = null;
            for (FastDispatcherType dispatcherType : dispatcherTypes) {
                jakarta.servlet.DispatcherType type = jakarta.servlet.DispatcherType.valueOf(dispatcherType.name());
                if (toDispatcherTypes == null) {
                    toDispatcherTypes = EnumSet.of(type);
                } else {
                    toDispatcherTypes.add(type);
                }
            }
            ((jakarta.servlet.FilterRegistration) target).addMappingForUrlPatterns(toDispatcherTypes, isMatchAfter, urlPatterns);
        }
    }

    public Collection<String> getUrlPatternMappings() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.FilterRegistration) target).getUrlPatternMappings();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((javax.servlet.FilterRegistration) target).getUrlPatternMappings();
        }
        return null;
    }

    public String getName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.FilterRegistration) target).getName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((javax.servlet.FilterRegistration) target).getName();
        }
        return null;
    }

    public String getClassName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.FilterRegistration) target).getClassName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((javax.servlet.FilterRegistration) target).getClassName();
        }
        return null;
    }

    public boolean setInitParameter(String name, String value) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.FilterRegistration) target).setInitParameter(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((javax.servlet.FilterRegistration) target).setInitParameter(name, value);
        }
        return false;
    }

    public String getInitParameter(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.FilterRegistration) target).getInitParameter(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((javax.servlet.FilterRegistration) target).getInitParameter(name);
        }
        return null;
    }

    public Set<String> setInitParameters(Map<String, String> initParameters) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.FilterRegistration) target).setInitParameters(initParameters);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((javax.servlet.FilterRegistration) target).setInitParameters(initParameters);
        }
        return null;
    }

    public Map<String, String> getInitParameters() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.FilterRegistration) target).getInitParameters();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((javax.servlet.FilterRegistration) target).getInitParameters();
        }
        return null;
    }


}
