package com.fastchar.interfaces;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.core.FastConstant;

/**
 * 数据加密接口
 */
@AFastPriority
public interface IFastSecurity {

    /**
     * md5加密数据
     * @param value 待加密的值
     * @return 加密后的数据
     */
    String MD5_Encrypt(String value);

    /**
     * aes加密数据
     * @param password 加密密码
     * @param value 待加密的值
     * @return 加密后的数据
     */
    String AES_Encrypt(String password, String value);

    /**
     * aes解密数据
     * @param password 解密密码
     * @param value 待解密的值
     * @return 解密后的数据
     */
    String AES_Decrypt(String password, String value);

    /**
     * rsa公钥加密数据
     * @param publicKey 加密的公钥
     * @param value 待加密的值
     * @return 加密后的数据
     */
    String RSA_Encrypt_PublicKey(String publicKey, String value);

    /**
     * rsa私钥加密数据
     * @param privateKey 加密的私钥
     * @param value 待加密的值
     * @return 加密后的数据
     */
    String RSA_Encrypt_PrivateKey(String privateKey, String value);

    /**
     * rsa公钥解密数据
     * @param publicKey 解密的公钥
     * @param value 待解密的值
     * @return 解密后的数据
     */
    String RSA_Decrypt_PublicKey(String publicKey, String value);

    /**
     * rsa私钥解密数据
     * @param privateKey 解密的私钥
     * @param value 待解密的值
     * @return 解密后的数据
     */
    String RSA_Decrypt_PrivateKey(String privateKey, String value);
}
