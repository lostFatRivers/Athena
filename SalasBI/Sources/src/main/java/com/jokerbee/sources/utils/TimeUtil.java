package com.jokerbee.sources.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    private static final String DAY_FORMAT = "yyyy-MM-dd";
    private static final String FULL_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String HOUR_TIME_FORMAT = "HH:mm:ss";
    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

    public static String getCurrentFormatDay() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DAY_FORMAT);
        return sdf.format(date);
    }

}
