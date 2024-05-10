package com.fastchar.acceptor;

import com.fastchar.core.FastEngine;
import com.fastchar.core.FastResource;
import com.fastchar.interfaces.IFastScannerAcceptor;

/**
 * web.xml文件扫描接收器
 */
public class FastJarResourceAcceptor implements IFastScannerAcceptor {
    @Override
    public void onScannerResource(FastEngine engine, FastResource file) throws Exception {
        if (file.isJarProtocol()) {
            engine.getJarResources().addResourcePath(file.getURL().toString());
        }
    }
}
