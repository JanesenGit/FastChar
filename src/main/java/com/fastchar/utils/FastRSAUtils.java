package com.fastchar.utils;

import com.fastchar.core.FastChar;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class FastRSAUtils {

    private static final String PADDING = "RSA/NONE/PKCS1Padding";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();

    /**
     * 根据公钥进行加密数据
     *
     * @param publicKey 公钥
     * @param content   待加密的内容
     * @return 加密后的内容
     */
    public static byte[] encryptByPublicKeyToBytes(String publicKey, String content) {
        return encryptByPublicKeyToBytes(publicKey, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据公钥进行加密数据
     *
     * @param publicKey 公钥
     * @param content   待加密的内容
     * @return 加密后的内容
     */
    public static byte[] encryptByPublicKeyToBytes(String publicKey, byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
            int inputLen = content.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(content, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(content, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();

            return encryptedData;
        } catch (Exception e) {
            FastChar.getLogger().error(FastRSAUtils.class, e);
        }
        return null;
    }

    /**
     * 根据公钥进行加密数据
     *
     * @param publicKey 公钥
     * @param content   待加密的内容
     * @return 加密后的内容
     */
    public static String encryptByPublicKey(String publicKey, String content) {
        return FastBase64Utils.encode(encryptByPublicKeyToBytes(publicKey, content));
    }

    /**
     * 根据公钥进行加密数据
     *
     * @param publicKey 公钥
     * @param content   待加密的内容
     * @return 加密后的内容
     */
    public static String encryptByPublicKey(String publicKey, byte[] content) {
        return FastBase64Utils.encode(encryptByPublicKeyToBytes(publicKey, content));
    }


    /**
     * 根据私钥加密数据
     *
     * @param privateKey 私钥pkcs8
     * @param content    待加密的内容
     * @return 加密后的内容
     */
    public static byte[] encryptByPrivateKeyToBytes(String privateKey, String content) {
        return encryptByPrivateKeyToBytes(privateKey, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据私钥加密数据
     *
     * @param privateKey 私钥pkcs8
     * @param content    待加密的内容
     * @return 加密后的内容
     */
    public static byte[] encryptByPrivateKeyToBytes(String privateKey, byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey(privateKey));
            int inputLen = content.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(content, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(content, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();
            return encryptedData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据私钥加密数据
     *
     * @param privateKey 私钥pkcs8
     * @param content    待加密的内容
     * @return 加密后的内容
     */
    public static String encryptByPrivateKey(String privateKey, String content) {
        return FastBase64Utils.encode(encryptByPrivateKeyToBytes(privateKey, content));
    }

    /**
     * 根据私钥加密数据
     *
     * @param privateKey 私钥pkcs8
     * @param content    待加密的内容
     * @return 加密后的内容
     */
    public static String encryptByPrivateKey(String privateKey, byte[] content) {
        return FastBase64Utils.encode(encryptByPrivateKeyToBytes(privateKey, content));
    }


    /**
     * 根据公钥解密
     *
     * @param publicKey 公钥
     * @param content   待解密的内容
     * @return 解密后的数据
     */
    public static byte[] decryptByPublicKeyToBytes(String publicKey, String content) {
        return decryptByPublicKeyToBytes(publicKey, FastBase64Utils.decodeToBytes(content));
    }


    /**
     * 根据公钥解密
     *
     * @param publicKey 公钥
     * @param content   待解密的内容
     * @return 解密后的数据
     */
    public static byte[] decryptByPublicKeyToBytes(String publicKey, byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, getPublicKey(publicKey));
            int inputLen = content.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(content, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(content, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            return decryptedData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据公钥解密
     *
     * @param publicKey 公钥
     * @param content   待解密的内容
     * @return 解密后的数据
     */
    public static String decryptByPublicKey(String publicKey, String content) {
        byte[] bytes = decryptByPublicKeyToBytes(publicKey, content);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 根据公钥解密
     *
     * @param publicKey 公钥
     * @param content   待解密的内容
     * @return 解密后的数据
     */
    public static String decryptByPublicKey(String publicKey, byte[] content) {
        byte[] bytes = decryptByPublicKeyToBytes(publicKey, content);
        return new String(bytes, StandardCharsets.UTF_8);
    }


    /**
     * 根据私钥解密
     *
     * @param privateKey 私钥pkcs8
     * @param content    待解密的内容
     * @return 解密后的数据
     */
    public static byte[] decryptByPrivateKeyToBytes(String privateKey, String content) {
        return decryptByPrivateKeyToBytes(privateKey, FastBase64Utils.decodeToBytes(content));
    }


    /**
     * 根据私钥解密
     *
     * @param privateKey 私钥pkcs8
     * @param content    待解密的内容
     * @return 解密后的数据
     */
    public static byte[] decryptByPrivateKeyToBytes(String privateKey, byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey));

            int inputLen = content.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(content, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(content, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            return decryptedData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据私钥解密
     *
     * @param privateKey 私钥pkcs8
     * @param content    待解密的内容
     * @return 解密后的数据
     */
    public static String decryptByPrivateKey(String privateKey, String content) {
        byte[] bytes = decryptByPrivateKeyToBytes(privateKey, content);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 根据私钥解密
     *
     * @param privateKey 私钥pkcs8
     * @param content    待解密的内容
     * @return 解密后的数据
     */
    public static String decryptByPrivateKey(String privateKey, byte[] content) {
        byte[] bytes = decryptByPrivateKeyToBytes(privateKey, content);
        return new String(bytes, StandardCharsets.UTF_8);
    }


    public static Key getPublicKey(String publicKey) {
        try {
            byte[] keyBytes = FastBase64Utils.decodeToBytes(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            FastChar.getLogger().error(FastRSAUtils.class, e);
        }
        return null;
    }


    public static Key getPrivateKey(String privateKey) {
        try {
            byte[] keyBytes = FastBase64Utils.decodeToBytes(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
