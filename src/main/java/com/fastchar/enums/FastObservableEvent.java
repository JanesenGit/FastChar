package com.fastchar.enums;

/**
 * @author Janesen
 */
public enum FastObservableEvent {
    /**
     * 当Web启动时
     */
    onWebStart("当Web启动时"),
    /**
     * 当Web停止时
     */
    onWebStop("当Web停止时"),
    /**
     * 当Web开始运行
     */
    onWebRun("当Web开始运行"),
    /**
     * 当Web已就绪
     */
    onWebReady("当Web已就绪"),
    /**
     * 当扫描结束
     */
    onScannerFinish("当扫描结束"),
    /**
     * 当数据初始化结束
     */
    onDatabaseFinish("当数据初始化结束")
    ;
    private final String details;

    FastObservableEvent(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}
