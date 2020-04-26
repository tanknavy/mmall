package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by Alex Cheng
 * 4/24/2020 9:29 PM
 */
public class DateTimeUtil {
    // joda-time
    // str->Date
    // Date->str
    public static final String STANDARD_FORMAT= "yyyy-MM-dd HH:mm:ss";

    public static Date str2Date(String dateTimeStr, String formatStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    //public static String date2Str(DateTime dateTime, String formatStr){
    public static String date2Str(Date date, String formatStr){
        if(date == null){
            return StringUtils.EMPTY;// ""空字符串，不是null
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }

    public static Date str2Date(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    //public static String date2Str(DateTime dateTime, String formatStr){
    public static String date2Str(Date date){
        if(date == null){
            return StringUtils.EMPTY;// ""空字符串，不是null
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }


    public static void main(String[] args) {
        System.out.println(DateTimeUtil.date2Str(new Date(), "yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateTimeUtil.str2Date("2020-04-24 21:50:06","yyyy-MM-dd HH:mm:ss"));
    }

}
