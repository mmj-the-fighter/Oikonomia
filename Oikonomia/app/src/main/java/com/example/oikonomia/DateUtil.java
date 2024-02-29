package com.example.oikonomia;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class DayMonthYear {
    public int day;
    public int month;
    public int year;
    public DayMonthYear(int d, int m, int y){
        day = d;
        month = m;
        year = y;
    }
    public DayMonthYear(DayMonthYear other){
        day = other.day;
        month = other.month;
        year = other.year;
    }
    public void copy(DayMonthYear other){
        day = other.day;
        month = other.month;
        year = other.year;
    }
}
public class DateUtil {

    private static boolean isValidDate(String date, SimpleDateFormat dateFormat) {
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isValidOikonomiaDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return isValidDate(date, dateFormat);
    }

    public static boolean isValidIsoDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return isValidDate(date, dateFormat);
    }

    public static String convertToOikonomiaDate(String isoDate) {
        if (isOikonomiaDate(isoDate)) {
            return isoDate;
        }
        String[] dateComponents = isoDate.split("-");
        //Oikonomia Format: dd/MM/yyyy
        //ISO8601: 2024-02-23
        String strOikonomiaDate = dateComponents[2] +
                "/" +
                dateComponents[1] +
                "/" +
                dateComponents[0];
        return strOikonomiaDate;
    }

    public static String convertToISO8601Date(String oikonomiaDate) {
        if (isIsoDate(oikonomiaDate)) {
            return oikonomiaDate;
        }
        String[] dateComponents = oikonomiaDate.split("/");
        //Oikonomia Format: dd/MM/yyyy
        //ISO8601: 2024-02-23
        String strDateISO8601 = dateComponents[2] +
                "-" +
                dateComponents[1] +
                "-" +
                dateComponents[0];
        return strDateISO8601;
    }

    public static boolean isIsoDate(String date) {
        return date.indexOf('-') > 0;
    }

    public static boolean isOikonomiaDate(String date) {
        return date.indexOf('/') > 0;
    }

    public static int compareDates(int dateYear, int dateMonth, int dateDay, int refDateYear, int refDateMonth, int refDateDay) {
        if (dateYear < refDateYear) {
            return -1;
        } else if (dateYear == refDateYear) {
            if (dateMonth < refDateMonth) {
                return -1;
            } else if (dateMonth == refDateMonth) {
                if (dateDay < refDateDay) {
                    return -1;
                }
            }
        }
        if (dateYear == refDateYear && dateMonth == refDateMonth && dateDay == refDateDay) {
            return 0;
        }
        return 1;
    }

    static DayMonthYear getDayMonthYearObjectForOikonomiaDate(String date){
        String[] components = date.split("/");
        DayMonthYear dmy = new DayMonthYear(Integer.parseInt(components[0]),
                Integer.parseInt(components[1]),
                Integer.parseInt(components[2])
                );
        return dmy;
    }

    static int compareIsoDateStrings(String date, String refDate) {
        String[] components = date.split("-");
        int dateYear = Integer.parseInt(components[0]);
        int dateMonth = Integer.parseInt(components[1]);
        int dateDay = Integer.parseInt(components[2]);
        components = refDate.split("-");
        int refDateYear = Integer.parseInt(components[0]);
        int refDateMonth = Integer.parseInt(components[1]);
        int refDateDay = Integer.parseInt(components[2]);
        int res = compareDates(dateYear, dateMonth, dateDay, refDateYear, refDateMonth, refDateDay);
        return res;
    }

    public static boolean isDateInRange(String isoDate, String startIsoDate, String endIsoDate) {
        String[] components = isoDate.split("-");
        int dateYear = Integer.parseInt(components[0]);
        int dateMonth = Integer.parseInt(components[1]);
        int dateDay = Integer.parseInt(components[2]);
        components = startIsoDate.split("-");
        int startDateYear = Integer.parseInt(components[0]);
        int startDateMonth = Integer.parseInt(components[1]);
        int startDateDay = Integer.parseInt(components[2]);
        components = endIsoDate.split("-");
        int endDateYear = Integer.parseInt(components[0]);
        int endDateMonth = Integer.parseInt(components[1]);
        int endDateDay = Integer.parseInt(components[2]);
        //check if left of time point 1
        int res1 = compareDates(dateYear, dateMonth, dateDay, startDateYear, startDateMonth, startDateDay);
        if (res1 < 0) {
            return false;
        }
        //check if right of time point 2
        int res2 = compareDates(dateYear, dateMonth, dateDay, endDateYear, endDateMonth, endDateDay);
        return res2 <= 0;
    }

    public static String getTimeStamp() {
        Calendar c = Calendar.getInstance();
        String s = c.getTime().toString();
        String r = s.replace(" ", "_")
                .replace(":", "_");
        return r;
    }

    public static String currentOikonomiaDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static String getOikonomiaDate(int dayOfMonth, int month, int year){
        StringBuilder sb = new StringBuilder();
        if(dayOfMonth < 10){
            sb.append("0");
            sb.append(dayOfMonth);
        }else{
            sb.append(dayOfMonth);
        }
        sb.append("/");
        if(month < 10){
            sb.append("0");
            sb.append(month);
        }else{
            sb.append(month);
        }
        sb.append("/");
        sb.append(year);
        return sb.toString();
    }

    public static DayMonthYear getDayMonthYearObjectForIsoDate(String isoDate) {
        String[] components = isoDate.split("-");
        DayMonthYear dmy = new DayMonthYear(Integer.parseInt(components[2]),
                Integer.parseInt(components[1]),
                Integer.parseInt(components[0])
        );
        return dmy;
    }
}