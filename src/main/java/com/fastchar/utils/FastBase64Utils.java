package com.fastchar.utils;

import com.fastchar.core.FastChar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class FastBase64Utils {
    private static final boolean hasUtilBase64;

    static {
        Class<?> aClass = FastClassUtils.getClass("java.util.Base64", false);
        hasUtilBase64 = aClass != null;
    }

    public static String encode(String content) {
        return encode(content.getBytes(StandardCharsets.UTF_8));
    }


    public static String encode(byte[] content) {
        if (hasUtilBase64) {
            return java.util.Base64.getMimeEncoder().encodeToString(content);
        } else {
            return new sun.misc.BASE64Encoder().encode(content);
        }
    }


    public static byte[] encodeToBytes(String content) {
        return encodeToBytes(content.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] encodeToBytes(byte[] content) {
        if (hasUtilBase64) {
            return java.util.Base64.getMimeEncoder().encode(content);
        } else {
            return new sun.misc.BASE64Encoder().encode(content).getBytes();
        }
    }


    public static String decode(String content){
        return decode(content.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(byte[] content){
        byte[] decode = decodeToBytes(content);
        return new String(decode, StandardCharsets.UTF_8);
    }


    public static byte[] decodeToBytes(String content){
        return decodeToBytes(content.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decodeToBytes(byte[] content){
        byte[] decode = new byte[0];
        if (hasUtilBase64) {
            decode = java.util.Base64.getMimeDecoder().decode(content);
        } else {
            try {
                decode = new sun.misc.BASE64Decoder().decodeBuffer(new ByteArrayInputStream(content));
            } catch (IOException e) {
                FastChar.getLogger().error(FastBase64Utils.class, e);
            }
        }
        return decode;
    }
}
