package com.fastchar.acceptor;

import com.fastchar.core.FastEngine;
import com.fastchar.core.FastEntity;
import com.fastchar.interfaces.IFastScannerAcceptor;


/**
 * 实体类接受扫描类
 * @author 沈建（Janesen）
 */
@SuppressWarnings("unchecked")
public class FastEntityScannerAcceptor implements IFastScannerAcceptor {

    @Override
    public void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        if (FastEntity.class.isAssignableFrom(scannedClass)) {
            engine.getEntities().addEntity((Class<? extends FastEntity<?>>) scannedClass);
        }
    }
}
