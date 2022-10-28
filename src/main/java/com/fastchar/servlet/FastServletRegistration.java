package com.fastchar.servlet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class FastServletRegistration {

    private final Object target;

    public FastServletRegistration(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public Set<String> addMapping(String... urlPatterns) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletRegistration) target).addMapping(urlPatterns);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletRegistration) target).addMapping(urlPatterns);
        }
        return null;
    }

    public Collection<String> getMappings() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletRegistration) target).getMappings();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletRegistration) target).getMappings();
        }
        return null;
    }

    public String getRunAsRole() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletRegistration) target).getRunAsRole();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletRegistration) target).getRunAsRole();
        }
        return null;
    }

    public String getName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletRegistration) target).getName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletRegistration) target).getName();
        }
        return null;
    }

    public String getClassName() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletRegistration) target).getClassName();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletRegistration) target).getClassName();
        }
        return null;
    }

    public boolean setInitParameter(String name, String value) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletRegistration) target).setInitParameter(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletRegistration) target).setInitParameter(name, value);
        }
        return false;
    }

    public String getInitParameter(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletRegistration) target).getInitParameter(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletRegistration) target).getInitParameter(name);
        }
        return null;
    }

    public Set<String> setInitParameters(Map<String, String> initParameters) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletRegistration) target).setInitParameters(initParameters);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletRegistration) target).setInitParameters(initParameters);
        }
        return null;
    }

    public Map<String, String> getInitParameters() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.ServletRegistration) target).getInitParameters();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.ServletRegistration) target).getInitParameters();
        }
        return null;
    }


}
