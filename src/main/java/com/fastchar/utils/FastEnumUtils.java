package com.fastchar.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class FastEnumUtils {


    public static <T extends Enum> T formatToEnum(Class<T> clazz, int index){
        return formatToEnum(clazz, index, null);
    }

    public static <T extends Enum> T formatToEnum(Class<T> clazz, int index, Enum defaultValue) {
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


    public static <T extends Enum> T formatToEnum(Class<T> clazz, String name){
        return formatToEnum(clazz, name, null);
    }

    public static <T extends Enum> T formatToEnum(Class<T> clazz, String name, Enum defaultValue) {
        try {
            if (FastStringUtils.isEmpty(name) || clazz == null) {
                return (T) defaultValue;
            }
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

    public static <T extends Enum<?>> T[] getEnumValues(Class<T> targetClass) {
        return targetClass.getEnumConstants();
    }

    public static <T extends Enum<?>> T[] getEnumValues(Class<T> targetClass, String... keys) {
        List<T> enumList = new ArrayList<>();
        T[] enumConstants = targetClass.getEnumConstants();
        for (T enumConstant : enumConstants) {
            for (String key : keys) {
                if (enumConstant.name().contains(key)) {
                    enumList.add(enumConstant);
                    break;
                }
            }
        }
        return enumList.toArray((T[]) Array.newInstance(targetClass, enumList.size()));
    }


    public static <T extends Enum<?>> T[] getEnumValuesAnd(Class<T> targetClass, String... keys) {
        List<T> enumList = new ArrayList<>();
        T[] enumConstants = targetClass.getEnumConstants();
        for (T enumConstant : enumConstants) {
            boolean hasKey = true;
            for (String key : keys) {
                if (!enumConstant.name().contains(key)) {
                    hasKey = false;
                    break;
                }
            }
            if (hasKey) {
                enumList.add(enumConstant);
            }
        }
        return enumList.toArray((T[]) Array.newInstance(targetClass, enumList.size()));
    }

    public static Integer[] getEnumOrdinals(Class<? extends  Enum<?>> targetClass, String... keys) {
        List<Integer> enumList = new ArrayList<>();
        Enum[] enumConstants = targetClass.getEnumConstants();
        for (Enum enumConstant : enumConstants) {
            for (String key : keys) {
                if (enumConstant.name().contains(key)) {
                    enumList.add(enumConstant.ordinal());
                    break;
                }
            }
        }
        return enumList.toArray(new Integer[]{});
    }


    public static Integer[] getEnumOrdinalsAnd(Class<? extends  Enum<?>> targetClass, String... keys) {
        List<Integer> enumList = new ArrayList<>();
        Enum[] enumConstants = targetClass.getEnumConstants();
        for (Enum enumConstant : enumConstants) {
            boolean hasKey = true;
            for (String key : keys) {
                if (!enumConstant.name().contains(key)) {
                    hasKey = false;
                    break;
                }
            }
            if (hasKey) {
                enumList.add(enumConstant.ordinal());
            }

        }
        return enumList.toArray(new Integer[]{});
    }

}
