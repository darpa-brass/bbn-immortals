package com.bbn.marti.util;

import javax.xml.bind.DatatypeConverter;
import java.text.SimpleDateFormat;
import java.util.SimpleTimeZone;

public class DateUtil {
    private static SimpleDateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        dateFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
    }

    public static final long millisFromTimeStr(String timeStr) {
        return DatatypeConverter.parseDateTime(timeStr).getTimeInMillis();
    }

    public static final String timeStrFromMillis(long millisSinceEpochUtc) {
        return dateFormat.format(millisSinceEpochUtc);
    }

    public static final void main(String[] args) {
        System.out.println(DateUtil.timeStrFromMillis(System.currentTimeMillis()));
    }

}
