package com.fastchar.observer;

import com.fastchar.core.FastChar;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.database.info.FastDatabaseInfo;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.interfaces.IFastDatabaseOperate;
import com.fastchar.utils.FastFileUtils;

import java.io.File;
import java.util.*;


public class FastDatabaseObserver {

    public void onScannerFinish() throws Exception {
        refreshDatabase();
    }


    private List<String> modifyTicket;

    private void restoreTicket() {
        try {
            File file = new File(FastChar.getPath().getClassRootPath(), ".fast_database");
            if (!file.exists()) {
                if (file.createNewFile()) {
                    try {
                        if (FastChar.getConstant().isLinux()) {
                            Runtime.getRuntime().exec("chmod -R 777 " + file.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    if (FastChar.getConstant().isDebug()) {
                        FastChar.getLog().error("文件" + file.getAbsolutePath() + "创建失败！");
                    }
                }
            }
            modifyTicket = FastFileUtils.readLines(file);
        } catch (Exception ignored) {}
        if (modifyTicket == null) {
            modifyTicket = new ArrayList<>();
        }
    }

    private void saveTicket() {
        try {
            File file = new File(FastChar.getPath().getClassRootPath(),".fast_database");
            FastFileUtils.writeLines(file, modifyTicket);
        } catch (Exception ignored) {}
    }


    public synchronized void refreshDatabase() throws Exception {
        restoreTicket();
        for (FastDatabaseInfo databaseInfo : FastChar.getDatabases().getAll()) {
            for (FastTableInfo<?> table : databaseInfo.getTables()) {
                table.validate();
                for (FastColumnInfo<?> column : table.getColumns()) {
                    column.validate();
                }
            }

            IFastDatabaseOperate databaseOperate = databaseInfo.getOperate();
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
                            if (checkIsModified(databaseInfo.getName(), table.getName(), column)) {
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
        saveTicket();
        if (FastChar.getDatabases().getAll().size() > 0) {
            FastChar.getObservable().notifyObservers("onDatabaseFinish");
        }
    }


    /**
     * 检测当前column是否被修改过
     * @return
     */
    private boolean checkIsModified(String databaseName,String tableName, FastColumnInfo columnInfo) {
        String key = FastChar.getSecurity().MD5_Encrypt(databaseName + "@" + tableName + "@" + columnInfo.getName());
        String value = FastChar.getSecurity().MD5_Encrypt(columnInfo.toString());
        boolean hasModified = true;
        boolean hasAdded = false;
        try {
            for (String string : modifyTicket) {
                if (string.startsWith(key)) {
                    hasAdded = true;
                    if (string.equals(key + "@" + value)) {
                        hasModified = false;
                    } else {
                        hasModified = true;
                        Collections.replaceAll(modifyTicket, string, key + "@" + value);
                    }
                    break;
                }
            }
            if (!hasAdded) {
                modifyTicket.add(key + "@" + value);
            }
        } catch (Exception e) {
            return false;
        }
        return hasModified;
    }


}
