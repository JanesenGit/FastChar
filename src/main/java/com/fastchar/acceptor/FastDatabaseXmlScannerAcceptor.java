package com.fastchar.acceptor;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastConstant;
import com.fastchar.core.FastEngine;
import com.fastchar.core.FastResource;
import com.fastchar.interfaces.IFastScannerAcceptor;
import com.fastchar.utils.FastStringUtils;

/**
 * fastchar-database-*.xml文件扫码接收器
 */
public class FastDatabaseXmlScannerAcceptor implements IFastScannerAcceptor {

    @Override
    public void onScannerResource(FastEngine engine, FastResource file) throws Exception {
        boolean xml = file.getName().toLowerCase().endsWith(".xml");
        String[] fast_data_base_file_prefix_array = FastStringUtils.splitByWholeSeparator(FastConstant.FAST_DATA_BASE_FILE_PREFIX, ",");
        for (String prefix : fast_data_base_file_prefix_array) {
            if (xml && file.getName().toLowerCase().startsWith(prefix)) {
                if (file.getName().toLowerCase().startsWith(prefix + "-")) {
                    engine.getDatabaseXml().parseDatabaseXml(file);
                } else {
                    FastChar.getLogger().warn(this.getClass(), "skip this file because there is no suffix ！[ " + file.getFile().getAbsolutePath() + " ]");
                }
                return;
            }
        }

        String[] fast_data_file_prefix_array = FastStringUtils.splitByWholeSeparator(FastConstant.FAST_DATA_FILE_PREFIX, ",");
        for (String prefix : fast_data_file_prefix_array) {
            if (xml && file.getName().toLowerCase().startsWith(prefix)) {
                if (file.getName().toLowerCase().startsWith(prefix + "-")) {
                    engine.getDatabaseXml().parseDataXml(file);
                } else {
                    FastChar.getLogger().warn(this.getClass(), "skip this file because there is no suffix ！[ " + file.getFile().getAbsolutePath() + " ]");
                }
                return;
            }
        }

    }
}
