package com.fastchar.accepter;

import com.fastchar.core.FastEngine;
import com.fastchar.core.FastEntity;
import com.fastchar.interfaces.IFastScannerAccepter;

import java.io.File;
import java.lang.reflect.Modifier;

@SuppressWarnings("unchecked")
public class FastEntityScannerAccepter implements IFastScannerAccepter {

    @Override
    public boolean onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        if (FastEntity.class.isAssignableFrom(scannedClass)) {
            engine.getEntities().addEntity((Class<? extends FastEntity>) scannedClass);
        }
        return true;
    }

    @Override
    public boolean onScannerFile(FastEngine engine, File file) throws Exception {
        return false;
    }
}
