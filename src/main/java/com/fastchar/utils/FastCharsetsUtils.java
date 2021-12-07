package com.fastchar.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FastCharsetsUtils {

    public static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;
    public static final Charset US_ASCII = StandardCharsets.US_ASCII;
    public static final Charset UTF_16 = StandardCharsets.UTF_16;
    public static final Charset UTF_16BE = StandardCharsets.UTF_16BE;
    public static final Charset UTF_16LE = StandardCharsets.UTF_16LE;
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    public FastCharsetsUtils() {
    }

    public static Charset toCharset(Charset charset) {
        return charset == null ? Charset.defaultCharset() : charset;
    }

    public static Charset toCharset(String charset) {
        return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
    }
}
