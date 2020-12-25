package com.fastchar.utils;

import com.fastchar.core.FastChar;
import com.fastchar.local.FastCharLocal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        if (FastStringUtils.isEmpty(pattern)) {
            return null;
        }
        return new SimpleDateFormat(pattern).format(new Date());
    }

    public static String format(Date date, String pattern) {
        if (date == null || FastStringUtils.isEmpty(pattern)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
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
        SimpleDateFormat sdf2 = new SimpleDateFormat(timePattern);
        int subDay = diffDay(dateTime, new Date());
        switch (subDay) {
            case 0:
                return FastChar.getLocal().getInfo(FastCharLocal.DATE_ERROR1) + sdf2.format(dateTime);
            case 1:
                return FastChar.getLocal().getInfo(FastCharLocal.DATE_ERROR2) + sdf2.format(dateTime);
            case 2:
                return FastChar.getLocal().getInfo(FastCharLocal.DATE_ERROR3) + sdf2.format(dateTime);
        }
        return format(dateTime, "yyyy-MM-dd ") + sdf2.format(dateTime);
    }

    public static int diffDay(Date first, Date two) {
        try {
            return (int) Math.abs( (first.getTime() - two.getTime()) / (60 * 60 * 1000 * 24));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int diffHour(Date first, Date two) {
        try {
            return (int) Math.abs((first.getTime() - two.getTime()) / (60 * 60 * 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int diffMinute(Date first, Date two) {
        try {
            return (int) Math.abs((first.getTime() - two.getTime()) / (60 * 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int diffSecond(Date first, Date two) {
        try {
            return (int) Math.abs((first.getTime() - two.getTime()) / (1000));
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


}
