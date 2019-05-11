package com.fastchar.utils;

import com.fastchar.core.FastChar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FastDateUtils {

    public static Date parse(String date, String pattern) {
        return parse(date, pattern, null);
    }

    public static Date parse(String date, String pattern, Date defaultValue){
        try {
            if (FastStringUtils.isEmpty(date)) {
                return defaultValue;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.parse(date);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getDateString() {
        return new SimpleDateFormat(FastChar.getConstant().getDateFormat()).format(new Date());
    }

    public static String getDateString(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }
}
