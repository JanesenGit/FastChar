package com.fastchar.database;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastResource;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.interfaces.IFastDatabaseXmlListener;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastDateUtils;
import com.fastchar.utils.FastStringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * fast-database.xml处理工具
 */
@SuppressWarnings("IOStreamConstructor")
public final class FastDatabaseXml {
    public static final String FAST_TAG_DATA_BASE = "database";
    public static final String FAST_TAG_TABLE = "table";
    public static final String FAST_TAG_COLUMN = "column";
    public static final String FAST_DATA = "data";

    public static final String ATTRIBUTE_VALUE_SECURITY_PREFIX = "SECURITY@";

    private final SAXParserFactory factory = SAXParserFactory.newInstance();
    private SAXParser parser;

    public FastDatabaseXml() {
        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://apache.org/xml/features/validation/schema", false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析fast-database.xml文件
     *
     * @param file fast-database.xml文件
     * @throws Exception 异常信息
     */
    public synchronized void parseDatabaseXml(FastResource file) throws Exception {
        if (file == null) {
            return;
        }

        if (parser == null) {
            parser = factory.newSAXParser();
        }
        List<IFastDatabaseXmlListener> iFastDatabaseXmlListeners = FastChar.getOverrides().singleInstances(false, IFastDatabaseXmlListener.class);
        for (IFastDatabaseXmlListener iFastDatabaseXmlListener : iFastDatabaseXmlListeners) {
            Boolean onBeforeParseDatabaseXml = iFastDatabaseXmlListener.onBeforeParseDatabaseXml(file);
            if (onBeforeParseDatabaseXml == null) {
                continue;
            }
            if (!onBeforeParseDatabaseXml) {
                return;
            }
        }

        DatabaseInfoHandler databaseInfoHandler = new DatabaseInfoHandler(file);
        parser.parse(file.getInputStream(), databaseInfoHandler);
        if (databaseInfoHandler.databaseInfo == null) {
            return;
        }

        for (IFastDatabaseXmlListener iFastDatabaseXmlListener : iFastDatabaseXmlListeners) {
            iFastDatabaseXmlListener.onAfterParseDatabaseXml(databaseInfoHandler);
        }

        String name = databaseInfoHandler.databaseInfo.getName();
        String code = databaseInfoHandler.databaseInfo.getCode();

        if (FastStringUtils.isEmpty(code)) {
            return;
        }

        databaseInfoHandler.databaseInfo.remove("name");
        databaseInfoHandler.databaseInfo.remove("code");
        boolean hasDatabaseMatch = false;
        for (FastDatabaseInfo databaseInfo : FastChar.getDatabases().getAll()) {
            if (FastStringUtils.matches(code, databaseInfo.getCode())) {
                databaseInfo.merge(databaseInfoHandler.databaseInfo.copy());
                hasDatabaseMatch = true;
            }
        }
        if (!hasDatabaseMatch) {
            databaseInfoHandler.databaseInfo.setName(name);
            databaseInfoHandler.databaseInfo.setCode(code);
            FastChar.getDatabases().add(databaseInfoHandler.databaseInfo);
        }

    }

    public synchronized void parseDataXml(FastResource file) throws Exception {
        if (file == null) {
            return;
        }
        if (parser == null) {
            parser = factory.newSAXParser();
        }
        DataInfoHandler dataInfoHandler = new DataInfoHandler();
        parser.parse(file.getInputStream(), dataInfoHandler);
    }

    public static class DatabaseInfoHandler extends DefaultHandler {
        private final FastResource xmlFile;
        private Locator locator;
        private FastDatabaseInfo databaseInfo;
        private FastTableInfo<?> tableInfo;
        private FastColumnInfo<?> columnInfo;

        public DatabaseInfoHandler(FastResource xmlFile) {
            this.xmlFile = xmlFile;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            super.setDocumentLocator(locator);
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (FAST_TAG_DATA_BASE.equalsIgnoreCase(qName)) {
                if (databaseInfo == null) {
                    databaseInfo = new FastDatabaseInfo();
                }
                databaseInfo.setFromXml(true);
                databaseInfo.setLineNumber(locator.getLineNumber());
                databaseInfo.setFileName(xmlFile.getName());
                databaseInfo.setLastModified(xmlFile.lastModified());
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attr = attributes.getQName(i);
                    String attrValue = getAttrValue(attributes, attr);
                    databaseInfo.put(attr.toLowerCase(), attrValue);
                }
            } else if (FAST_TAG_TABLE.equalsIgnoreCase(qName)) {
                tableInfo = FastTableInfo.newInstance();
                if (tableInfo == null) {
                    return;
                }
                tableInfo.setFromXml(true);
                tableInfo.setLineNumber(locator.getLineNumber());
                tableInfo.setFileName(xmlFile.getName());

                for (int i = 0; i < attributes.getLength(); i++) {
                    String attr = attributes.getQName(i);
                    String attrValue = getAttrValue(attributes, attr);
                    tableInfo.put(attr.toLowerCase(), attrValue);
                }

                if (FastStringUtils.isNotEmpty(tableInfo.getData())) {
                    File dataFile = new File(xmlFile.getParent(), tableInfo.getData());
                    if (dataFile.exists()) {
                        tableInfo.setData(dataFile.getAbsolutePath());
                    } else {
                        if (FastChar.getConstant().isDebug()) {
                            FastChar.getLogger().warn(this.getClass(), FastChar.getLocal().getInfo(FastCharLocal.FILE_ERROR7, dataFile.getAbsolutePath()));
                        }
                    }
                }
            } else if (FAST_TAG_COLUMN.equalsIgnoreCase(qName)) {
                columnInfo = FastColumnInfo.newInstance();
                if (columnInfo == null) {
                    return;
                }
                columnInfo.setFromXml(true);
                columnInfo.setLineNumber(locator.getLineNumber());
                columnInfo.setFileName(xmlFile.getName());

                for (int i = 0; i < attributes.getLength(); i++) {
                    String attr = attributes.getQName(i);
                    String attrValue = getAttrValue(attributes, attr);
                    columnInfo.put(attr.toLowerCase(), attrValue);
                }
            }
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (FAST_TAG_DATA_BASE.equalsIgnoreCase(qName)) {
                //do nothing
            } else if (FAST_TAG_TABLE.equalsIgnoreCase(qName)) {
                tableInfo.setDatabase(databaseInfo.getCode());
                databaseInfo.addTable(tableInfo);
            } else if (FAST_TAG_COLUMN.equalsIgnoreCase(qName)) {
                columnInfo.setTableName(tableInfo.getName());
                columnInfo.setDatabase(tableInfo.getDatabase());
                tableInfo.addColumn(columnInfo);
            }
        }

        String getAttrValue(Attributes attributes, String attr) {
            String value = attributes.getValue(attr);
            if (!value.startsWith(ATTRIBUTE_VALUE_SECURITY_PREFIX)) {
                return value;
            }
            value = value.replace(ATTRIBUTE_VALUE_SECURITY_PREFIX, "");
            if (FastChar.getSecurity() == null) {
                return value;
            }
            //尝试解密
            String decrypt = FastChar.getSecurity().AES_Decrypt(FastChar.getConstant().getEncryptPassword(), value);
            if (FastStringUtils.isNotEmpty(decrypt)) {
                value = decrypt;
            }
            return value;
        }
    }


    public static class DataInfoHandler extends DefaultHandler {
        String database = "*";


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (qName.equalsIgnoreCase(FAST_DATA)) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attrName = attributes.getQName(i);
                    String value = attributes.getValue(attrName);
                    if (FAST_TAG_DATA_BASE.equalsIgnoreCase(attrName)) {
                        database = value;
                    }
                }
            } else {
                FastSqlInfo sqlInfo = FastSqlInfo.newInstance();
                List<String> columns = new ArrayList<String>();
                List<String> placeholders = new ArrayList<String>();
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attrName = attributes.getQName(i);
                    String value = attributes.getValue(attrName);
                    if (FAST_TAG_DATA_BASE.equalsIgnoreCase(attrName)) {
                        database = value;
                        continue;
                    }

                    columns.add(attrName);
                    if (value.startsWith("@")) {
                        if ("@now".equalsIgnoreCase(value)) {
                            placeholders.add("?");
                            sqlInfo.getParams().add(FastDateUtils.getDateString());
                        }
                    } else {
                        placeholders.add("?");
                        sqlInfo.getParams().add(value);
                    }
                }
                sqlInfo.setSql("insert into " + qName + "(" + FastStringUtils.join(columns, ",") + ") values (" + FastStringUtils.join(placeholders, ',') + ");");
                for (FastDatabaseInfo databaseInfo : FastChar.getDatabases().getAll()) {
                    if (FastStringUtils.matches(database, databaseInfo.getCode())) {
                        if (!databaseInfo.getDefaultData().containsKey(qName)) {
                            databaseInfo.getDefaultData().put(qName, new ArrayList<>(10));
                        }
                        databaseInfo.getDefaultData().get(qName).add(sqlInfo.copy().setType(databaseInfo.getType()));
                    }
                }
            }
        }

    }

}
