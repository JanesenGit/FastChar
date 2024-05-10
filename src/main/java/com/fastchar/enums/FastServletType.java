package com.fastchar.enums;

/**
 * 启动项目的 servlet类型
 */
public enum FastServletType {
    None(""),
    Javax("javax.servlet.ServletContainerInitializer"),
    Jakarta("jakarta.servlet.ServletContainerInitializer"),
    ;
    private final String targetClass;

    FastServletType(String targetClass) {
        this.targetClass = targetClass;
    }

    public String getTargetClass() {
        return targetClass;
    }
}
