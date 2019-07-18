/**
 * 安徽创息软件科技有限公司版权所有 http://www.croshe.com
 **/
package com.fastchar.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastNumberUtils {

    private static boolean isMatch(String regex, String orginal) {
        if (orginal == null || orginal.trim().equals("")) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher isNum = pattern.matcher(orginal);
        return isNum.matches();
    }

    /**
     * 是否是正整数
     * @param orginal
     * @return
     */
    public static boolean isPositiveInteger(String orginal) {
        return isMatch("^\\+{0,1}[1-9]\\d*", orginal);
    }

    /**
     * 是否是负整数
     * @param orginal
     * @return
     */
    public static boolean isNegativeInteger(String orginal) {
        return isMatch("^-[1-9]\\d*", orginal);
    }

    /**
     * 是否是数字，不包含小数点
     * @param orginal
     * @return
     */
    public static boolean isWholeNumber(String orginal) {
        return isMatch("[+-]{0,1}0", orginal) || isPositiveInteger(orginal) || isNegativeInteger(orginal);
    }

    /**
     * 是否是含有小数点的正数字
     * @param orginal
     * @return
     */
    public static boolean isPositiveDecimal(String orginal) {
        return isMatch("\\+{0,1}[0]\\.[1-9]*|\\+{0,1}[1-9]\\d*\\.\\d*", orginal);
    }

    /**
     * 是否是含有小数点的负数字
     * @param orginal
     * @return
     */
    public static boolean isNegativeDecimal(String orginal) {
        return isMatch("^-[0]\\.[1-9]*|^-[1-9]\\d*\\.\\d*", orginal);
    }

    /**
     * 是否是含有小数点的数字
     * @param orginal
     * @return
     */
    public static boolean isDecimal(String orginal) {
        return isMatch("[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+", orginal);
    }

    /**
     * 是否是数字
     * @param orginal
     * @return
     */
    public static boolean isRealNumber(String orginal) {
        return isWholeNumber(orginal) || isDecimal(orginal);
    }


    /**
     * 字符转数字
     * @param value
     * @return
     */
    public static Number formatToNumber(final Object value) {
        return formatToNumber(value, 0);
    }


    /**
     * 字符转数字
     * @param value
     * @return
     */
    public static Number formatToNumber(final Object value, final Number defaultValue) {
        try {
            if (value == null || value.toString().length() == 0) return defaultValue;
            Number number = new Number() {
                private static final long serialVersionUID = 1L;
                String numberStr = "";

                @Override
                public long longValue() {
                    try {
                        numberStr = value.toString().replace(" ", "");
                        if (isDecimal(numberStr)) {
                            return (long) Float.parseFloat(numberStr);
                        } else {
                            return Long.parseLong(numberStr);
                        }
                    } catch (Exception e) {
                        return defaultValue.longValue();
                    }
                }

                @Override
                public int intValue() {
                    try {
                        numberStr = value.toString().replace(" ", "");
                        if (isDecimal(numberStr)) {
                            return (int) Float.parseFloat(numberStr);
                        } else {
                            return Integer.parseInt(numberStr);
                        }
                    } catch (Exception e) {
                        return defaultValue.intValue();
                    }
                }

                @Override
                public float floatValue() {
                    try {
                        numberStr = value.toString().replace(" ", "");
                        return Float.parseFloat(numberStr);
                    } catch (Exception e) {
                        return defaultValue.floatValue();
                    }
                }

                @Override
                public double doubleValue() {
                    try {
                        numberStr = value.toString().replace(" ", "");
                        return Double.parseDouble(numberStr);
                    } catch (Exception e) {
                        return defaultValue.doubleValue();
                    }
                }
            };
            return number;
        } catch (Exception e) {
            return defaultValue;
        }
    }


    public static int formatToInt(Object value) {
        return formatToNumber(value).intValue();
    }


    public static int formatToInt(Object value, int defaultValue) {
        return formatToNumber(value, defaultValue).intValue();
    }


    public static short formatToShort(Object value) {
        return formatToNumber(value).shortValue();
    }


    public static short formatToShort(Object value, short defaultValue) {
        return formatToNumber(value, defaultValue).shortValue();
    }


    public static float formatToFloat(Object value) {
        return formatToNumber(value).floatValue();
    }

    public static float formatToFloat(Object value, float defaultValue) {
        return formatToNumber(value, defaultValue).floatValue();
    }


    public static double formatToDouble(Object value) {
        return formatToNumber(value).doubleValue();
    }

    public static double formatToDouble(Object value, double defaultValue) {
        return formatToNumber(value, defaultValue).doubleValue();
    }

    public static double formatToDouble(Object value, int digit) {
        try {
            return new BigDecimal(String.valueOf(formatToDouble(value))).setScale(digit,
                    BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public static double formatToDouble(Object value, double defaultValue, int digit) {
        try {
            return new BigDecimal(String.valueOf(formatToDouble(value, defaultValue))).setScale(digit,
                    BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public static float formatToFloat(Object value, int digit) {
        try {
            return new BigDecimal(String.valueOf(formatToFloat(value))).setScale(digit,
                    BigDecimal.ROUND_HALF_UP).floatValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public static float formatToFloat(Object value, float defaultValue, int digit) {
        try {
            return new BigDecimal(String.valueOf(formatToFloat(value,defaultValue))).setScale(digit,
                    BigDecimal.ROUND_HALF_UP).floatValue();
        } catch (Exception e) {
            return 0;
        }
    }




    public static long formatToLong(Object value) {
        return formatToNumber(value).longValue();
    }

    public static long formatToLong(Object value, long defaultValue) {
        return formatToNumber(value, defaultValue).longValue();
    }


    /**
     * 获取字符串中的所有数字
     * @param content
     * @return
     */
    public static String getAllNumbers(String content) {
        List<String> numbers = new ArrayList<String>();
        for (String n : content.replaceAll("[^0-9]", ",").split(",")) {
            if (n.length() > 0)
                numbers.add(n);
        }
        return FastStringUtils.join(numbers, "");
    }


    /**
     * 数字格式化
     * @param value
     * @return
     */
    public static String numberFormat(Object value, int digit) {
        String pattern = "#.";
        String zStr = "";
        for (int i = 0; i < digit; i++) {
            zStr += "0";
        }
        DecimalFormat df = new DecimalFormat(pattern + zStr);
        return df.format(value);
    }


    /**
     * 数字格式化
     * @param value
     * @param pattern 规则 例如 ###.00、###,###.0
     * @return
     */
    public static String numberFormat(Object value, String pattern) {
        DecimalFormat format = new DecimalFormat(pattern);
        return format.format(value);
    }



    /**
     * 计算需要显示的页码
     * @param currPage 当前页
     * @param totalPage 总页数
     * @param visibleCount 显示页数数量
     * @return
     */
    public static List<Integer> computePages(int currPage, int totalPage, int visibleCount) {
        int leftCount = visibleCount / 2;
        List<Integer> pages = new ArrayList<Integer>();
        if (currPage <= leftCount + 1) {
            if (visibleCount > totalPage) {
                for (int i = 1; i <= totalPage; i++) {
                    pages.add(i);
                }
            } else {
                for (int i = 1; i <= visibleCount; i++) {
                    pages.add(i);
                }
            }
        } else {
            for (int i = currPage - leftCount; i <= currPage + leftCount; i++) {
                if (i <= totalPage) {
                    pages.add(i);
                }
            }

            if (pages.size() < visibleCount) {
                int span = visibleCount - pages.size();
                for (int i = 0; i < span; i++) {
                    pages.add(0, pages.get(0) - 1);
                }
            }
        }
        return pages;
    }


    /**
     * 计算百分比后的数字
     */
    public static float formatToPercentage(Object value) {
        return formatToFloat(value) / 100;
    }



    /**
     * 将数字转成字符串
     * @param number
     * @return
     */
    public static String toPlainText(Object number) {
        if (number == null) {
            return null;
        }
        BigDecimal bd = new BigDecimal(number.toString());
        return bd.toPlainString();
    }


    /**
     * 随机数
     * @param minValue
     * @param maxValue
     * @return
     */
    public static double random(double minValue, double maxValue) {
        Random r = new Random();
        return Math.max(r.nextDouble() * maxValue, minValue);
    }

}

