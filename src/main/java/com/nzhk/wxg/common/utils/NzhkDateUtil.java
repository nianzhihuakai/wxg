package com.nzhk.wxg.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NzhkDateUtil {

    public static  DateTimeFormatter secondsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getDateTimeStrByLocalDateTime (LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return localDateTime.format(secondsFormatter);
    }

}
