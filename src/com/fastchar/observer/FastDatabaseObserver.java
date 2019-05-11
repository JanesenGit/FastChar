package com.fastchar.observer;

import com.fastchar.core.FastChar;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.interfaces.IFastDatabaseOperateProvider;
import com.fastchar.utils.FastFileUtils;

import java.io.File;
import java.util.*;


public class FastDatabaseObserver {

    public void onWebStart() throws Exception {
        this.refreshDatabase();
    }


    private synchronized void refreshDatabase() throws Exception {
        for (FastDatabaseInfo databaseInfo : FastChar.getDatabases().getAll()) {
            for (FastTableInfo<?> table : databaseInfo.getTables()) {
                table.validate();
                for (FastColumnInfo<?> column : table.getColumns()) {
                    column.validate();
                }
            }

            IFastDatabaseOperateProvider databaseOperate = databaseInfo.getOperate();
            if (databaseOperate == null) {
                continue;
            }
            if (FastChar.getConstant().isSyncDatabaseXml()) {
                //本地xml配置同步到数据库中
                databaseOperate.createDatabase(databaseInfo);
                for (FastTableInfo<?> table : databaseInfo.getTables()) {
                    if (!databaseOperate.checkTableExists(databaseInfo, table)) {
                        databaseOperate.createTable(databaseInfo, table);
                    }
                    for (FastColumnInfo<?> column : table.getColumns()) {
                        if (databaseOperate.checkColumnExists(databaseInfo, table, column)) {
                            if (checkIsModified(databaseInfo.getName(),table.getName(), column)) {
                                databaseOperate.alterColumn(databaseInfo, table, column);
                            }
                            continue;
                        }
                        databaseOperate.addColumn(databaseInfo, table, column);
                        checkIsModified(databaseInfo.getName(), table.getName(), column);
                    }
                }

                databaseOperate.fetchDatabaseInfo(databaseInfo);
            }
        }
    }


    /**
     * 检测当前column是否被修改过
     *
     * @return
     */
    private boolean checkIsModified(String databaseName,String tableName, FastColumnInfo columnInfo) {
        String key = FastChar.getSecurity().MD5_Encrypt(databaseName + "@" + tableName + "@" + columnInfo.getName());
        String value = FastChar.getSecurity().MD5_Encrypt(columnInfo.toString());
        boolean hasModified = true;
        boolean hasAdded = false;
        try {
            File file = new File(FastChar.getPath().getClassRootPath() + "/.fast_database");
            List<String> strings = new ArrayList<>();
            if (file.exists()) {
                strings = FastFileUtils.readLines(file);
            } else if (file.createNewFile()) {
                try {
                    String string = " attrib +H " + file.getAbsolutePath(); //设置文件属性为隐藏
                    Runtime.getRuntime().exec(string);
                } catch (Exception ignored) {
                }
            }
            for (String string : strings) {
                if (string.startsWith(key)) {
                    hasAdded = true;
                    if (string.equals(key + "@" + value)) {
                        hasModified = false;
                    } else {
                        hasModified = true;
                        Collections.replaceAll(strings, string, key + "@" + value);
                    }
                    break;
                }
            }
            if (!hasAdded) {
                strings.add(key + "@" + value);
            }
            FastFileUtils.writeLines(file, strings);
        } catch (Exception e) {
            return false;
        }
        return hasModified;
    }


}
