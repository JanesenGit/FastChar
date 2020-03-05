package com.fastchar.accepter;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEngine;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.interfaces.IFastScannerAccepter;
import com.fastchar.utils.FastDateUtils;
import com.fastchar.utils.FastStringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
        if (xml && file.getName().toLowerCase().startsWith("fast-database")) {
            engine.getDatabaseXml().parseDatabaseXml(file);
            return true;
        } else if (xml && file.getName().toLowerCase().startsWith("fast-data")) {
            engine.getDatabaseXml().parseDataXml(file);
            return true;
        }
        return false;
    }

}
