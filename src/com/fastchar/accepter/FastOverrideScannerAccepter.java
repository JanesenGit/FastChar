package com.fastchar.accepter;

import com.fastchar.annotation.AFastOverride;
import com.fastchar.core.FastEngine;
import com.fastchar.interfaces.IFastScannerAccepter;

import java.io.File;

public class FastOverrideScannerAccepter implements IFastScannerAccepter {
    @Override
    public boolean onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        if (scannedClass.isAnnotationPresent(AFastOverride.class)) {
            engine.getOverrides().add(scannedClass);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScannerFile(FastEngine engine, File file) throws Exception {
        return false;
    }
}
