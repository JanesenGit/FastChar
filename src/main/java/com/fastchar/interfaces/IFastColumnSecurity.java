package com.fastchar.interfaces;

import com.fastchar.database.info.FastColumnInfo;

public interface IFastColumnSecurity {

    String encrypt(FastColumnInfo columnInfo, String value);

    String decrypt(FastColumnInfo columnInfo, String value);

}
