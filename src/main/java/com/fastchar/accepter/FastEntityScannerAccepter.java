package com.fastchar.accepter;

import com.fastchar.core.FastEngine;
import com.fastchar.core.FastEntity;
import com.fastchar.interfaces.IFastScannerAccepter;

import java.io.File;


/**
 * 实体类接受扫描类
 * @author 沈建（Janesen）
 */
@SuppressWarnings("unchecked")
public class FastEntityScannerAccepter implements IFastScannerAccepter {

    @Override
    public void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        if (FastEntity.class.isAssignableFrom(scannedClass)) {
            engine.getEntities().addEntity((Class<? extends FastEntity<?>>) scannedClass);
        }
    }

    @Override
    public void onScannerFile(FastEngine engine, File file) throws Exception {
    }
}
