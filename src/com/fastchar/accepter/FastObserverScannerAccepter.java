package com.fastchar.accepter;

import com.fastchar.annotation.AFastObserver;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastEngine;
import com.fastchar.interfaces.IFastScannerAccepter;

import java.io.File;
import java.lang.reflect.Modifier;

@SuppressWarnings("unchecked")
public class FastObserverScannerAccepter implements IFastScannerAccepter {
    @Override
    public boolean onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        if (Modifier.isInterface(scannedClass.getModifiers())) {
            return false;
        }
        if (Modifier.isAbstract(scannedClass.getModifiers())) {
            return false;
        }
        if (!Modifier.isPublic(scannedClass.getModifiers())) {
            return false;
        }
        if (scannedClass.isAnnotationPresent(AFastObserver.class)) {
            if (engine.getObservable().containerObserver(scannedClass)) {
                return true;
            }
            engine.getObservable().addObserver(scannedClass.newInstance());
            return true;
        }
        return false;
    }

    @Override
    public boolean onScannerFile(FastEngine engine, File file) throws Exception {
        return false;
    }

}
