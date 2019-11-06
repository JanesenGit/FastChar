/**
 * 安徽创息软件科技有限公司版权所有 http://www.croshe.com
 **/
package com.fastchar.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MD5加密
 *
 */
public class FastMD5Utils {

    public static String MD5(Object data) {
        String s = String.valueOf(data);
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            if (s != null && !"".equals(s.trim())) {
                byte[] strTemp = s.getBytes(StandardCharsets.UTF_8);
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
        return data.toString();
    }

}