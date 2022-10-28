package com.fastchar.enums;

/**
 * 启动项目的 servlet类型
 */
public enum FastServletType {
    JAVAX("javax.servlet.*"),
    JAKARTA("jakarta.servlet.*"),
    ;
    private final String details;

    FastServletType(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}
