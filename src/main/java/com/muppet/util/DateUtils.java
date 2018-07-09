package com.muppet.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuhaiqiang on 2018/7/8.
 *
 * @description
 */

public class DateUtils {

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Date convertStr2Date(String dateTimeStr, String format) throws ParseException {
        return new SimpleDateFormat(format).parse(dateTimeStr);
    }

    public static Date convertStr2Date(String dateTimeStr) throws ParseException {
        return convertStr2Date(dateTimeStr, DEFAULT_FORMAT);
    }

    public static String format(Date date) {
        return new SimpleDateFormat(DEFAULT_FORMAT).format(date);
    }
}

