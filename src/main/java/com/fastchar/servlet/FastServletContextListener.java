package com.fastchar.servlet;

import java.util.EventListener;

public abstract class FastServletContextListener implements EventListener{

    @SuppressWarnings("unchecked")
    public <T extends EventListener> T createTarget() {
        if (FastServletHelper.isJavaxServlet()) {
            return (T) new javax.servlet.ServletContextListener() {
                @Override
                public void contextInitialized(javax.servlet.ServletContextEvent sce) {
                    FastServletContextListener.this.contextInitialized(FastServletContextEvent.newInstance(sce));
                }

                @Override
                public void contextDestroyed(javax.servlet.ServletContextEvent sce) {
                    FastServletContextListener.this.contextDestroyed(FastServletContextEvent.newInstance(sce));
                }
            };
        }
        if (FastServletHelper.isJakartaServlet()) {
            return (T) new jakarta.servlet.ServletContextListener() {
                @Override
                public void contextInitialized(jakarta.servlet.ServletContextEvent sce) {
                    FastServletContextListener.this.contextInitialized(FastServletContextEvent.newInstance(sce));
                }

                @Override
                public void contextDestroyed(jakarta.servlet.ServletContextEvent sce) {
                    FastServletContextListener.this.contextDestroyed(FastServletContextEvent.newInstance(sce));
                }
            };
        }
        return null;
    }


    public abstract void contextInitialized(FastServletContextEvent sce);

    public abstract void contextDestroyed(FastServletContextEvent sce);
}
