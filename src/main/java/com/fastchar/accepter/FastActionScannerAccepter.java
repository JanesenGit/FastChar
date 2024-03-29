package com.fastchar.accepter;

import com.fastchar.core.*;
import com.fastchar.interfaces.IFastScannerAccepter;

import java.io.File;

/**
 * FastAction类扫码接收器，自动注册FastAction类
 */
@SuppressWarnings("unchecked")
public class FastActionScannerAccepter implements IFastScannerAccepter {
    @Override
    public void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        if (FastAction.class.isAssignableFrom(scannedClass)) {
            engine.getActions().add((Class<? extends FastAction>) scannedClass);
        }
    }

    @Override
    public void onScannerFile(FastEngine engine, File file) throws Exception {
    }
}
