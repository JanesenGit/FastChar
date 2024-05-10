package com.fastchar.acceptor;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastEngine;
import com.fastchar.interfaces.IFastScannerAcceptor;

/**
 * FastAction类扫码接收器，自动注册FastAction类
 */
@SuppressWarnings("unchecked")
public class FastActionScannerAcceptor implements IFastScannerAcceptor {
    @Override
    public void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        if (FastAction.class.isAssignableFrom(scannedClass)) {
            engine.getActions().add((Class<? extends FastAction>) scannedClass);
        }
    }
}
