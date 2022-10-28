/**
 * 安徽创息软件科技有限公司版权所有 http://www.croshe.com
 **/
package com.fastchar.utils;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * 常规的AES加解密工具类，AES规则：128位、ECB、PKCS5Padding 、UTF-8 ,如果需要设置AES的iv规则自行编写工具类！
 */
public class FastAESUtils {

    /**
     * 加密，默认AES填充类型：AES/ECB/PKCS5Padding
     *
     * @param content  待加密的内容
     * @param password 加密秘钥
     * @return 加密后的内容
     */
    public static String encrypt(String content, String password) {
        return encrypt("AES/ECB/PKCS5Padding", content, password);
    }

    /**
     * 加密，128位
     *
     * @param paddingType AES加密填充类型
     * @param content     待加密的内容
     * @param password    加密秘钥
     * @return 加密后的内容
     */
    public static String encrypt(String paddingType, String content, String password) {
        return encrypt(paddingType, content, buildSecretKey(password));
    }

    /**
     * 加密
     *
     * @param paddingType AES加密填充类型
     * @param content     待加密的内容
     * @param password    加密秘钥，对象
     * @return 加密后的内容
     */
    public static String encrypt(String paddingType, String content, SecretKey password) {
        try {
            Cipher cipher = Cipher.getInstance(paddingType);
            cipher.init(Cipher.ENCRYPT_MODE, password);
            byte[] byte_encode = content.getBytes(StandardCharsets.UTF_8);
            byte[] byte_AES = cipher.doFinal(byte_encode);
            return FastBase64Utils.encode(byte_AES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密，128位，默认AES填充类型：AES/ECB/PKCS5Padding
     *
     * @param content  待解密的内容
     * @param password 解密秘钥
     * @return 解密后的内容
     */
    public static String decrypt(String content, String password) {
        return decrypt("AES/ECB/PKCS5Padding", content, password);
    }

    /**
     * 解密，128位
     *
     * @param paddingType AES解密填充类型
     * @param content     待解密的内容
     * @param password    解密秘钥
     * @return 解密后的内容
     */
    public static String decrypt(String paddingType, String content, String password) {
        return decrypt(paddingType, content, buildSecretKey(password));
    }


    /**
     * 解密
     *
     * @param paddingType AES解密填充类型
     * @param content     待解密的内容
     * @param password    解密秘钥对象
     * @return 解密后的内容
     */
    public static String decrypt(String paddingType, String content, SecretKey password) {
        try {
            Cipher cipher = Cipher.getInstance(paddingType);
            cipher.init(Cipher.DECRYPT_MODE, password);
            byte[] byte_content = FastBase64Utils.decodeToBytes(content);
            byte[] byte_decode = cipher.doFinal(byte_content);
            return new String(byte_decode, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static SecretKey buildSecretKey(String password) {
        try {
            if (password.length() < 16) {
                //当密码长度小于要求的16位时，使用提供的密码字符生成唯一的秘钥
                KeyGenerator keygen = KeyGenerator.getInstance("AES");
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

                //提供生成秘钥的字符因子
                random.setSeed(password.getBytes());
                keygen.init(128, random);

                SecretKey newKey = keygen.generateKey();
                return new SecretKeySpec(newKey.getEncoded(), "AES");
            }
            return new SecretKeySpec(password.getBytes(), "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
