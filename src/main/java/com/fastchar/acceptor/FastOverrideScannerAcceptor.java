package com.fastchar.acceptor;

import com.fastchar.annotation.AFastOverride;
import com.fastchar.core.FastEngine;
import com.fastchar.interfaces.IFastScannerAcceptor;

/**
 * 注解AFastOverride扫描接收器，检测到AFastOverride注解的类将自动注册到类代理器中
 */
public class FastOverrideScannerAcceptor implements IFastScannerAcceptor {
    @Override
    public void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        if (scannedClass.isAnnotationPresent(AFastOverride.class)) {
            engine.getOverrides().add(scannedClass);
        }
    }
}
