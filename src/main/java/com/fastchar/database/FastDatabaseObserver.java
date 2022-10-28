package com.fastchar.database;

import com.fastchar.core.FastChar;


public class FastDatabaseObserver {

    public void onScannerFinish() throws Exception {
        FastChar.getDatabases().flushDatabase();
    }

}
