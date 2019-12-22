package com.fastchar.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * from org.apache.commons.lang3
 */
public class FastStringUtils {
    static final AtomicInteger atomicInteger = new AtomicInteger();

    /**
     * 插入字符串
     *
     * @param string
     * @param position
     * @param insert
     * @return
     */
    public static String insertString(String string, int position, String insert) {
        if (position < 0) {
            return string + insert;
        }
        String char1 = string.substring(0, position);
        String char2 = string.substring(position);
        return char1 + insert + char2;
    }


    /**
     * 插入字符串
     *
     * @param string
     * @param insert
     * @return
     */
    public static String insertString(String string, String split, String insert) {
        return insertString(string, string.indexOf(split), insert);
    }


    /**
     * 首字母大写
     *
     * @param str
     * @return
     */
    public static String firstCharToUpper(String str) {
        if (Character.isLowerCase(str.charAt(0))) {
            char[] array = str.toCharArray();
            array[0] -= 32;
            return String.valueOf(array);
        }
        return str;
    }

    /**
     * 首字母小写
     *
     * @param str
     * @return
     */
    public static String firstCharToLower(String str) {
        if (Character.isUpperCase(str.charAt(0))) {
            char[] array = str.toCharArray();
            array[0] += 32;
            return String.valueOf(array);
        }
        return str;
    }


    public static String join(Iterable<?> iterable, char separator) {
        return iterable == null ? null : join(iterable.iterator(), separator);
    }

    public static String join(Iterable<?> iterable, String separator) {
        return iterable == null ? null : join(iterable.iterator(), separator);
    }


    public static String join(Object[] array, char separator) {
        return array == null ? null : join((Object[]) array, separator, 0, array.length);
    }

    public static String join(long[] array, char separator) {
        return array == null ? null : join((long[]) array, separator, 0, array.length);
    }

    public static String join(int[] array, char separator) {
        return array == null ? null : join((int[]) array, separator, 0, array.length);
    }

    public static String join(short[] array, char separator) {
        return array == null ? null : join((short[]) array, separator, 0, array.length);
    }

    public static String join(byte[] array, char separator) {
        return array == null ? null : join((byte[]) array, separator, 0, array.length);
    }

    public static String join(char[] array, char separator) {
        return array == null ? null : join((char[]) array, separator, 0, array.length);
    }

    public static String join(float[] array, char separator) {
        return array == null ? null : join((float[]) array, separator, 0, array.length);
    }

    public static String join(double[] array, char separator) {
        return array == null ? null : join((double[]) array, separator, 0, array.length);
    }

    public static String join(Object[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = startIndex; i < endIndex; ++i) {
                    if (i > startIndex) {
                        buf.append(separator);
                    }

                    if (array[i] != null) {
                        buf.append(array[i]);
                    }
                }

                return buf.toString();
            }
        }
    }

    public static String join(long[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = startIndex; i < endIndex; ++i) {
                    if (i > startIndex) {
                        buf.append(separator);
                    }

                    buf.append(array[i]);
                }

                return buf.toString();
            }
        }
    }

    public static String join(int[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = startIndex; i < endIndex; ++i) {
                    if (i > startIndex) {
                        buf.append(separator);
                    }

                    buf.append(array[i]);
                }

                return buf.toString();
            }
        }
    }

    public static String join(byte[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = startIndex; i < endIndex; ++i) {
                    if (i > startIndex) {
                        buf.append(separator);
                    }

                    buf.append(array[i]);
                }

                return buf.toString();
            }
        }
    }

    public static String join(short[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = startIndex; i < endIndex; ++i) {
                    if (i > startIndex) {
                        buf.append(separator);
                    }

                    buf.append(array[i]);
                }

                return buf.toString();
            }
        }
    }

    public static String join(char[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = startIndex; i < endIndex; ++i) {
                    if (i > startIndex) {
                        buf.append(separator);
                    }

                    buf.append(array[i]);
                }

                return buf.toString();
            }
        }
    }

    public static String join(double[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = startIndex; i < endIndex; ++i) {
                    if (i > startIndex) {
                        buf.append(separator);
                    }

                    buf.append(array[i]);
                }

                return buf.toString();
            }
        }
    }

    public static String join(float[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = startIndex; i < endIndex; ++i) {
                    if (i > startIndex) {
                        buf.append(separator);
                    }

                    buf.append(array[i]);
                }

                return buf.toString();
            }
        }
    }

    public static String join(Object[] array, String separator) {
        return array == null ? null : join(array, separator, 0, array.length);
    }


    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else {
            if (separator == null) {
                separator = "";
            }

            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for (int i = startIndex; i < endIndex; ++i) {
                    if (i > startIndex) {
                        buf.append(separator);
                    }

                    if (array[i] != null) {
                        buf.append(array[i]);
                    }
                }

                return buf.toString();
            }
        }
    }

    public static String join(Iterator<?> iterator, char separator) {
        if (iterator == null) {
            return null;
        } else if (!iterator.hasNext()) {
            return "";
        } else {
            Object first = iterator.next();
            if (!iterator.hasNext()) {
                String result = String.valueOf(first);
                return result;
            } else {
                StringBuilder buf = new StringBuilder(256);
                if (first != null) {
                    buf.append(first);
                }

                while (iterator.hasNext()) {
                    buf.append(separator);
                    Object obj = iterator.next();
                    if (obj != null) {
                        buf.append(obj);
                    }
                }

                return buf.toString();
            }
        }
    }

    public static String join(Iterator<?> iterator, String separator) {
        if (iterator == null) {
            return null;
        } else if (!iterator.hasNext()) {
            return "";
        } else {
            Object first = iterator.next();
            if (!iterator.hasNext()) {
                String result = String.valueOf(first);
                return result;
            } else {
                StringBuilder buf = new StringBuilder(256);
                if (first != null) {
                    buf.append(first);
                }

                while (iterator.hasNext()) {
                    if (separator != null) {
                        buf.append(separator);
                    }

                    Object obj = iterator.next();
                    if (obj != null) {
                        buf.append(obj);
                    }
                }

                return buf.toString();
            }
        }
    }


    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isAnyEmpty(CharSequence... css) {
        if (css.length == 0) {
            return true;
        } else {
            CharSequence[] arr$ = css;
            int len$ = css.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                CharSequence cs = arr$[i$];
                if (isEmpty(cs)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean isNoneEmpty(CharSequence... css) {
        return !isAnyEmpty(css);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isAnyBlank(CharSequence... css) {
        if (css.length == 0) {
            return true;
        } else {
            CharSequence[] arr$ = css;
            int len$ = css.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                CharSequence cs = arr$[i$];
                if (isBlank(cs)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean isNoneBlank(CharSequence... css) {
        return !isAnyBlank(css);
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static String trimToNull(String str) {
        String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    public static String strip(String str) {
        return strip(str, (String) null);
    }

    public static String stripToNull(String str) {
        if (str == null) {
            return null;
        } else {
            str = strip(str, (String) null);
            return str.isEmpty() ? null : str;
        }
    }

    public static String stripToEmpty(String str) {
        return str == null ? "" : strip(str, (String) null);
    }

    public static String strip(String str, String stripChars) {
        if (isEmpty(str)) {
            return str;
        } else {
            str = stripStart(str, stripChars);
            return stripEnd(str, stripChars);
        }
    }

    public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            int start = 0;
            if (stripChars == null) {
                while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                    ++start;
                }
            } else {
                if (stripChars.isEmpty()) {
                    return str;
                }

                while (start != strLen && stripChars.indexOf(str.charAt(start)) != -1) {
                    ++start;
                }
            }

            return str.substring(start);
        } else {
            return str;
        }
    }

    public static String stripEnd(String str, String stripChars) {
        int end;
        if (str != null && (end = str.length()) != 0) {
            if (stripChars == null) {
                while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                    --end;
                }
            } else {
                if (stripChars.isEmpty()) {
                    return str;
                }

                while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                    --end;
                }
            }

            return str.substring(0, end);
        } else {
            return str;
        }
    }

    public static String[] stripAll(String... strs) {
        return stripAll(strs, (String) null);
    }

    public static String[] stripAll(String[] strs, String stripChars) {
        int strsLen;
        if (strs != null && (strsLen = strs.length) != 0) {
            String[] newArr = new String[strsLen];

            for (int i = 0; i < strsLen; ++i) {
                newArr[i] = strip(strs[i], stripChars);
            }

            return newArr;
        } else {
            return strs;
        }
    }

    public static String stripAccents(String input) {
        if (input == null) {
            return null;
        } else {
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
            return pattern.matcher(decomposed).replaceAll("");
        }
    }


    public static String defaultValue(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (isEmpty(String.valueOf(value))) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return FastNumberUtils.toPlainText(value);
        }
        return String.valueOf(value);
    }


    /**
     * 是否匹配
     * @param pattern    可使用通配符* 例如：/druid/*
     * @param target 目标路径
     * @return
     */
    public static boolean matches(String pattern, String target) {
        if (isEmpty(pattern) || isEmpty(target)) {
            return false;
        }
        String reg = pattern.replace(".", "\\.").replace("*", ".*");
        return Pattern.matches(reg, target);
    }


    public static synchronized String buildOnlyCode(String prefix) {
        if (isEmpty(prefix)) {
            prefix = "";
        }
        if (atomicInteger.get() > 999) {
            atomicInteger.set(1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");//精确到毫秒
        return prefix + sdf.format(new Date()) + String.format("%03d", atomicInteger.incrementAndGet());
    }

    public static String buildUUID() {
        return UUID.randomUUID().toString();
    }


    public static int countChar(String source, char c) {
        int count = 0;
        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == c) count++;
        }
        return count;
    }


    public static String wrap(String str, char wrapWith) {
        return !isEmpty(str) && wrapWith != 0 ? wrapWith + str + wrapWith : str;
    }

    public static String wrap(String str, String wrapWith) {
        return !isEmpty(str) && !isEmpty(wrapWith) ? wrapWith.concat(str).concat(wrapWith) : str;
    }

    public static int truthLength(String content) {
        try {
            return new String(content.getBytes("GBK"), StandardCharsets.ISO_8859_1).length();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return content.length();
    }
}
