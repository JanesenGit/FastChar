package com.fastchar.accepter;

import com.fastchar.core.FastConstant;
import com.fastchar.core.FastEngine;
import com.fastchar.interfaces.IFastScannerAccepter;
import com.fastchar.utils.FastStringUtils;

import java.io.File;

/**
 * fast-database文件扫码接收器
 */
public class FastDatabaseXmlScannerAccepter implements IFastScannerAccepter {

    @Override
    public void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
    }

    @Override
    public void onScannerFile(FastEngine engine, File file) throws Exception {
        boolean xml = file.getName().toLowerCase().endsWith(".xml");

        String[] fast_data_base_file_prefix_array = FastStringUtils.splitByWholeSeparator(FastConstant.FAST_DATA_BASE_FILE_PREFIX,",");
        for (String prefix : fast_data_base_file_prefix_array) {
            if (xml && file.getName().toLowerCase().startsWith(prefix + "-")) {
                engine.getDatabaseXml().parseDatabaseXml(file);
                return;
            }
        }

        String[] fast_data_file_prefix_array = FastStringUtils.splitByWholeSeparator(FastConstant.FAST_DATA_FILE_PREFIX,",");
        for (String prefix : fast_data_file_prefix_array) {
            if (xml && file.getName().toLowerCase().startsWith(prefix + "-")) {
                engine.getDatabaseXml().parseDataXml(file);
                return ;
            }
        }
    }

}
