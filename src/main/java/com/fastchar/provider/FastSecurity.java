package com.fastchar.provider;

import com.fastchar.interfaces.IFastSecurity;
import com.fastchar.utils.FastAESUtils;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastRSAUtils;


public class FastSecurity implements IFastSecurity {
    @Override
    public String MD5_Encrypt(String value) {
        return FastMD5Utils.MD5(value);
    }

    @Override
    public String AES_Encrypt(String password,String value) {
        return FastAESUtils.encrypt(value,password);
    }

    @Override
    public String AES_Decrypt(String password,String value) {
        return FastAESUtils.decrypt(value, password);
    }

    @Override
    public String RSA_Encrypt_PublicKey(String publicKey, String value) {
        return FastRSAUtils.encryptByPublicKey(publicKey, value);
    }

    @Override
    public String RSA_Encrypt_PrivateKey(String privateKey, String value) {
        return FastRSAUtils.encryptByPrivateKey(privateKey, value);
    }

    @Override
    public String RSA_Decrypt_PublicKey(String publicKey, String value) {
        return FastRSAUtils.decryptByPublicKey(publicKey, value);
    }

    @Override
    public String RSA_Decrypt_PrivateKey(String privateKey, String value) {
        return FastRSAUtils.decryptByPrivateKey(privateKey, value);
    }
}
