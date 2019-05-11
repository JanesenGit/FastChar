package com.fastchar.provider;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastSecurityProvider;
import com.fastchar.utils.FastAESUtils;
import com.fastchar.utils.FastMD5Utils;


public class FastSecurityProvider implements IFastSecurityProvider {
    @Override
    public String MD5_Encrypt(String value) {
        return FastMD5Utils.MD5(value);
    }

    @Override
    public String AES_Encrypt(String value) {
        return FastAESUtils.encrypt(value, FastChar.getConstant().getEncryptPassword());
    }

    @Override
    public String AES_Decrypt(String value) {
        return FastAESUtils.decrypt(value, FastChar.getConstant().getEncryptPassword());
    }

    @Override
    public String RSA_Encrypt(String value) {
        return null;
    }

    @Override
    public String RSA_Decrypt(String value) {
        return null;
    }
}
