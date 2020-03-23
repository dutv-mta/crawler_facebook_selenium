package com.dutv.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtils {
    public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static LocalDate toLocalDate(Date date) {
        LocalDate local = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return local;
    }

    public static Date toDate(LocalDateTime lcDateTime) {
        Date date = Date.from(lcDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }
}
