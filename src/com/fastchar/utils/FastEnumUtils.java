package com.fastchar.utils;

import java.util.List;

@SuppressWarnings("unchecked")
public class FastEnumUtils {


    public static <T extends Enum> T formatToEnum(Class<? extends Enum> clazz, int index){
        return formatToEnum(clazz, index, null);
    }

    public static <T extends Enum> T formatToEnum(Class<? extends Enum> clazz, int index, Enum defaultValue) {
        try {
            Enum[] c = clazz.getEnumConstants();
            if (index >= c.length) {
                return (T) defaultValue;
            }
            return (T) c[index];
        } catch (Exception ignored) {
        }
        return (T) defaultValue;
    }


    public static <T extends Enum> T formatToEnum(Class<? extends Enum> clazz, String name){
        return formatToEnum(clazz, name, null);
    }

    public static <T extends Enum> T formatToEnum(Class<? extends Enum> clazz, String name, Enum defaultValue) {
        try {
            int index = FastNumberUtils.formatToInt(name, -1);
            if (index >= 0) {
                return formatToEnum(clazz, index, defaultValue);
            }
            return (T) Enum.valueOf(clazz, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) defaultValue;
    }


    public static <T extends Enum> T[] getEnumValues(Class<? extends Enum> targetClass) {
        return (T[]) targetClass.getEnumConstants();
    }

}
