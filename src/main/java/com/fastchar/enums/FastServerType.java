package com.fastchar.enums;

/**
 * 启动项目的容器类型
 */
public enum FastServerType {
    None(""),
    Tomcat("org.apache.catalina.core.StandardContext"),
    Jetty("org.eclipse.jetty.server.Server"),
    Undertow("io.undertow.Undertow"),
    ;
    private final String details;

    FastServerType(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}
