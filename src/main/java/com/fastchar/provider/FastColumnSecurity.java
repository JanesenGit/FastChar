package com.fastchar.provider;

import com.fastchar.core.FastChar;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.interfaces.IFastColumnSecurity;
import com.fastchar.utils.FastStringUtils;

public class FastColumnSecurity implements IFastColumnSecurity {
    @Override
    public String encrypt(FastColumnInfo columnInfo, String value) {
        if (FastStringUtils.isEmpty(value)) {
            return null;
        }
        if (columnInfo.getEncrypt().equalsIgnoreCase("md5")) {
            return FastChar.getSecurity().MD5_Encrypt(value);
        } else if (columnInfo.getEncrypt().equalsIgnoreCase("true")) {
            return FastChar.getSecurity().AES_Encrypt(FastChar.getConstant().getEncryptPassword(), value);
        }
        return value;
    }

    @Override
    public String decrypt(FastColumnInfo columnInfo, String value) {
        if (FastStringUtils.isEmpty(value)) {
            return null;
        }
        if (columnInfo.getEncrypt().equalsIgnoreCase("true")) {
            return FastChar.getSecurity().AES_Decrypt(FastChar.getConstant().getEncryptPassword(), value);
        }
        return value;
    }
}
