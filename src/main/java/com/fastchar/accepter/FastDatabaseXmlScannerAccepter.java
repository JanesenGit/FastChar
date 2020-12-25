package com.fastchar.accepter;

import com.fastchar.core.FastConstant;
import com.fastchar.core.FastEngine;
import com.fastchar.interfaces.IFastScannerAccepter;

import java.io.File;

/**
 * fast-database文件扫码接收器
 */
public class FastDatabaseXmlScannerAccepter implements IFastScannerAccepter {

    @Override
    public boolean onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
        return false;
    }

    @Override
    public boolean onScannerFile(FastEngine engine, File file) throws Exception {
        boolean xml = file.getName().toLowerCase().endsWith(".xml");
        if (xml && file.getName().toLowerCase().startsWith(FastConstant.FAST_DATA_BASE_FILE_PREFIX)) {
            engine.getDatabaseXml().parseDatabaseXml(file);
            return true;
        } else if (xml && file.getName().toLowerCase().startsWith(FastConstant.FAST_DATA_FILE_PREFIX)) {
            engine.getDatabaseXml().parseDataXml(file);
            return true;
        }
        return false;
    }

}
