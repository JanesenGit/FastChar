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
     * @return 布尔值
     */
    public static boolean formatToBoolean(Object value,boolean defaultValue) {
        if (value != null) {
            try {
                String string = String.valueOf(value).replace(" ", "");
                if (FastStringUtils.isEmpty(string)) {
                    return defaultValue;
                }
                if ("true".equalsIgnoreCase(string)) {
                    return true;
                }
                if ("false".equalsIgnoreCase(string)) {
                    return false;
                }
                int intValue = FastNumberUtils.formatToInt(string, -1);
                if (intValue == 0) {
                    return false;
                }
                if (intValue == 1) {
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return defaultValue;
    }

}
