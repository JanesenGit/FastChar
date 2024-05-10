package com.fastchar.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastConsoleUtils {
    private static final Pattern REG_COLOR_STYLE = Pattern.compile("\u001b\\[38;5;([0-9]+)m(.*)\u001b\\[0m", Pattern.DOTALL);
    private static final Pattern REG_COLOR_STYLE2 = Pattern.compile("\u001b\\[1m(.*)\u001b\\[0m", Pattern.DOTALL);


    /**
     * 颜色打印控制台
     *
     * @param colorValue 颜色值 1-256
     * @param content    打印的内容
     * @return 打印的内容
     */
    public static String colorStyle(int colorValue, String content) {
        if (FastStringUtils.isEmpty(content)) {
            return null;
        }
        if (colorValue <= 0) {
            return content;
        }
        return "\u001b[38;5;" + colorValue + "m" + content + "\u001b[0m";
    }

    /**
     * 移除content中的颜色值【注意，只适用于使用当前类增加样式后的内容】
     */
    public static String trimStyleContent(String content) {
        if (FastStringUtils.isEmpty(content)) {
            return null;
        }
        Matcher matcher = REG_COLOR_STYLE.matcher(content);
        if (matcher.find()) {
            return matcher.group(2);
        }
        matcher = REG_COLOR_STYLE2.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return content;
    }

    public static String boldStyle(String content) {
        if (FastStringUtils.isEmpty(content)) {
            return null;
        }
        return "\u001b[1m" + content + "\u001b[0m";
    }

    public static String lightStyle(String content) {
        return colorStyle(34, content);
    }

    public static String softStyle(String content) {
        return colorStyle(4, content);
    }

    public static String warnStyle(String content) {
        return colorStyle(11, content);
    }

    public static String errorStyle(String content) {
        return colorStyle(9, content);
    }

}
