package com.fastchar.observer;

import com.fastchar.core.FastChar;


public class FastDatabaseObserver {

    public void onScannerFinish() throws Exception {
        FastChar.getDatabases().flushDatabase();
    }

}
