package com.fastchar.utils;

import com.fastchar.core.FastChar;
import com.fastchar.extend.commons.lang3.time.DateFormatUtils;
import com.fastchar.extend.commons.lang3.time.DateUtils;
import com.fastchar.local.FastCharLocal;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class FastDateUtils {

    public static final Map<Pattern, String> DATE_FORMAT_MAP = new LinkedHashMap<>(16);

    static {
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"), "yyyy-MM-dd HH:mm:ss");
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}"), "yyyy-MM-dd HH:mm");
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}"), "yyyy-MM-dd HH");
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}"), "yyyy-MM-dd");

        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}/[0-9]{2}/[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"), "yyyy/MM/dd HH:mm:ss");
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}/[0-9]{2}/[0-9]{2} [0-9]{2}:[0-9]{2}"), "yyyy/MM/dd HH:mm");
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}/[0-9]{2}/[0-9]{2} [0-9]{2}"), "yyyy/MM/dd HH");
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}/[0-9]{2}/[0-9]{2}"), "yyyy/MM/dd");


        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}年[0-9]{2}月[0-9]{2}日 [0-9]{2}:[0-9]{2}:[0-9]{2}"), "yyyy年MM月dd日 HH:mm:ss");
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}年[0-9]{2}月[0-9]{2}日 [0-9]{2}:[0-9]{2}"), "yyyy年MM月dd日 HH:mm");
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}年[0-9]{2}月[0-9]{2}日 [0-9]{2}"), "yyyy年MM月dd日 HH");
        DATE_FORMAT_MAP.put(Pattern.compile("[0-9]{4}年[0-9]{2}月[0-9]{2}日"), "yyyy年MM月dd日");


    }


    public static Date parse(String date, String pattern) {
        return parse(date, pattern, null);
    }

    public static Date parse(String date, String pattern, Date defaultValue) {
        try {
            if (FastStringUtils.isEmpty(date)) {
                return defaultValue;
            }
            return DateUtils.parseDate(date, pattern);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getDateString() {
        return DateFormatUtils.format(new Date(), FastChar.getConstant().getDateFormat());
    }

    public static String getDateString(String pattern) {
        if (FastStringUtils.isEmpty(pattern)) {
            return null;
        }
        return DateFormatUtils.format(new Date(), pattern);
    }

    public static String format(Date date, String pattern) {
        if (date == null || FastStringUtils.isEmpty(pattern)) {
            return null;
        }
        return DateFormatUtils.format(date, pattern);
    }


    public static Date addYears(Date date, int amount) {
        return add(date, 1, amount);
    }

    public static Date addMonths(Date date, int amount) {
        return add(date, 2, amount);
    }

    public static Date addWeeks(Date date, int amount) {
        return add(date, 3, amount);
    }

    public static Date addDays(Date date, int amount) {
        return add(date, 5, amount);
    }

    public static Date addHours(Date date, int amount) {
        return add(date, 11, amount);
    }

    public static Date addMinutes(Date date, int amount) {
        return add(date, 12, amount);
    }

    public static Date addSeconds(Date date, int amount) {
        return add(date, 13, amount);
    }

    public static Date addMilliseconds(Date date, int amount) {
        return add(date, 14, amount);
    }

    private static Date add(Date date, int calendarField, int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        } else {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(calendarField, amount);
            return c.getTime();
        }
    }

    public static Date setYears(Date date, int amount) {
        return set(date, 1, amount);
    }

    public static Date setMonths(Date date, int amount) {
        return set(date, 2, amount);
    }

    public static Date setDays(Date date, int amount) {
        return set(date, 5, amount);
    }

    public static Date setHours(Date date, int amount) {
        return set(date, 11, amount);
    }

    public static Date setMinutes(Date date, int amount) {
        return set(date, 12, amount);
    }

    public static Date setSeconds(Date date, int amount) {
        return set(date, 13, amount);
    }

    public static Date setMilliseconds(Date date, int amount) {
        return set(date, 14, amount);
    }

    private static Date set(Date date, int calendarField, int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        } else {
            Calendar c = Calendar.getInstance();
            c.setLenient(false);
            c.setTime(date);
            c.set(calendarField, amount);
            return c.getTime();
        }
    }

    public static Calendar toCalendar(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }


    public static String smartDateString(Date dateTime, String timePattern) {
        if (dateTime == null) {
            return "";
        }
        int subDay = (int) diffDay(dateTime, new Date());
        switch (subDay) {
            case 0:
                return FastChar.getLocal().getInfo(FastCharLocal.DATE_ERROR1) + DateFormatUtils.format(dateTime, timePattern);
            case 1:
                return FastChar.getLocal().getInfo(FastCharLocal.DATE_ERROR2) + DateFormatUtils.format(dateTime, timePattern);
            case 2:
                return FastChar.getLocal().getInfo(FastCharLocal.DATE_ERROR3) + DateFormatUtils.format(dateTime, timePattern);
        }
        return format(dateTime, "yyyy-MM-dd ") + DateFormatUtils.format(dateTime, timePattern);
    }

    public static double diffDay(Date first, Date two) {
        try {
            return Math.abs(first.getTime() - two.getTime()) / (60.0 * 60 * 1000 * 24);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double diffHour(Date first, Date two) {
        try {
            return Math.abs(first.getTime() - two.getTime()) / (60.0 * 60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double diffMinute(Date first, Date two) {
        try {
            return Math.abs(first.getTime() - two.getTime()) / (60.0 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double diffSecond(Date first, Date two) {
        try {
            return Math.abs(first.getTime() - two.getTime()) / 1000.0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int diffYear(Date first, Date two) {
        try {
            Calendar firstCal = Calendar.getInstance();
            firstCal.setTime(first);

            Calendar twoCal = Calendar.getInstance();
            twoCal.setTime(two);
            return Math.abs(firstCal.get(Calendar.YEAR) - twoCal.get(Calendar.YEAR));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static String guessDateFormat(String dateValue) {
        if (FastStringUtils.isEmpty(dateValue)) {
            return null;
        }
        for (Map.Entry<Pattern, String> stringStringEntry : DATE_FORMAT_MAP.entrySet()) {
            if (stringStringEntry.getKey().matcher(dateValue.trim()).matches()) {
                return stringStringEntry.getValue();
            }
        }
        return null;
    }

}
