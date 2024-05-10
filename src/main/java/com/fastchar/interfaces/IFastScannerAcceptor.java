package com.fastchar.interfaces;

import com.fastchar.core.FastEngine;
import com.fastchar.core.FastResource;

/**
 * 扫描接收器接口
 */
public interface IFastScannerAcceptor {

    /**
     * 扫描到class类触发
     *
     * @param engine       FastChar核心引擎
     * @param scannedClass 扫描到的类
     * @throws Exception 异常
     */
    default void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {

    }


    /**
     * 扫描到文件资源
     * @param engine FastChar核心引擎
     * @param file 扫描到的文件资源
     * @throws Exception 异常
     */
    default void onScannerResource(FastEngine engine, FastResource file) throws Exception {

    }


}
