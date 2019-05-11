package com.fastchar.utils;

public class FastBooleanUtils {

    /**
     * 格式化boolean值
     * @param value false true or  0 1
     * @return
     */
    public static boolean formatToBoolean(Object value) {
        return formatToBoolean(value, false);
    }

    /**
     * 格式化boolean值
     * @param value false true or  0 1
     * @return
     */
    public static boolean formatToBoolean(Object value,boolean defaultValue) {
        if (value != null) {
            try {
                int intValue = FastNumberUtils.formatToInt(value, -1);
                if (intValue == 0) {
                    return false;
                }
                if (intValue == 1) {
                    return true;
                }
                return Boolean.valueOf(value.toString().trim());
            } catch (Exception ignored) {}
        }
        return defaultValue;
    }

}
