package com.fastchar.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MD5加密
 */
public class FastMD5Utils {

    public static String MD5To16(Object data) {
        return MD5(data).substring(8, 24);
    }

    public static String MD5(Object data) {
        String content = String.valueOf(data);
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            if (content != null && !content.trim().isEmpty()) {
                byte[] strTemp = content.getBytes(StandardCharsets.UTF_8);
                MessageDigest mdTemp = MessageDigest.getInstance("MD5");
                mdTemp.update(strTemp);
                byte[] md = mdTemp.digest();
                int j = md.length;
                char[] str = new char[j * 2];
                int k = 0;
                for (byte byte0 : md) {
                    str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                    str[k++] = hexDigits[byte0 & 0xf];
                }
                return new String(str);
            } else {
                return "";
            }
        } catch (Exception ignored) {
        }
        return content;
    }


    public static String SHA1(Object data) {
        String content = String.valueOf(data);
        if (content == null || content.isEmpty()) {
            return "";
        }
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(content.getBytes(StandardCharsets.UTF_8));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] buf = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception ignored) {
        }
        return content;
    }
}