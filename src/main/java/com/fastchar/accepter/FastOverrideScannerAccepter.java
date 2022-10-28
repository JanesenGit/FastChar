package com.fastchar.accepter;

import com.fastchar.annotation.AFastOverride;
import com.fastchar.core.FastEngine;
import com.fastchar.interfaces.IFastScannerAccepter;

import java.io.File;

/**
 * 注解AFastOverride扫描接收器，检测到AFastOverride注解的类将自动注册到类代理器中
 */
public class FastOverrideScannerAccepter implements IFastScannerAccepter {
    @Override
    public void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        if (scannedClass.isAnnotationPresent(AFastOverride.class)) {
            engine.getOverrides().add(scannedClass);
        }
    }

    @Override
    public void onScannerFile(FastEngine engine, File file) throws Exception {
    }
}
