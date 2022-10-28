package com.fastchar.interfaces;

import com.fastchar.core.FastEngine;

import java.io.File;

/**
 * 扫描接收器接口
 */
public interface IFastScannerAccepter {

    /**
     * 扫描到class类触发
     * @param engine FastChar核心引擎
     * @param scannedClass 扫描到的类
     * @throws Exception 异常
     */
    void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception;

    /**
     * 扫描到文件触发
     * @param engine FastChar核心引擎
     * @param file 扫描到的文件
     * @throws Exception 异常
     */
    void onScannerFile(FastEngine engine, File file) throws Exception;

}
