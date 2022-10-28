package com.fastchar.database.provider;

import com.fastchar.core.FastChar;
import com.fastchar.database.info.FastColumnInfo;
import com.fastchar.interfaces.IFastColumnSecurity;
import com.fastchar.utils.FastStringUtils;

public class FastColumnSecurity implements IFastColumnSecurity {
    @Override
    public String encrypt(FastColumnInfo<?> columnInfo, String value) {
        if (FastStringUtils.isEmpty(value)) {
            return null;
        }
        if ("md5".equalsIgnoreCase(columnInfo.getEncrypt())) {
            return FastChar.getSecurity().MD5_Encrypt(value);
        } else if ("true".equalsIgnoreCase(columnInfo.getEncrypt())) {
            return FastChar.getSecurity().AES_Encrypt(FastChar.getConstant().getEncryptPassword(), value);
        }
        return value;
    }

    @Override
    public String decrypt(FastColumnInfo<?> columnInfo, String value) {
        if (FastStringUtils.isEmpty(value)) {
            return null;
        }
        if ("true".equalsIgnoreCase(columnInfo.getEncrypt())) {
            return FastChar.getSecurity().AES_Decrypt(FastChar.getConstant().getEncryptPassword(), value);
        }
        return value;
    }
}
