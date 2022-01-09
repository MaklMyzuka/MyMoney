package muzdima.mymoney.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTime {

    public int year;
    public int month;
    public int day;
    public int hours;
    public int minutes;
    public int seconds;

    public DateTime(int year, int month, int day, int hours, int minutes, int seconds) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public LocalDateTime toLocalDateTime(){
        return LocalDateTime.of(this.year, this.month, this.day, this.hours, this.minutes, this.seconds);
    }

    public static long getNowUTC() {
        return Instant.now().getEpochSecond();
    }

    private static int getLocalValueOfPatternFromUTC(String pattern, long utc) {
        return Integer.parseInt(DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault()).format(Instant.ofEpochSecond(utc)));
    }

    @NonNull
    public static DateTime convertUTCToLocal(long utc) {
        int year = getLocalValueOfPatternFromUTC("yyyy", utc);
        int month = getLocalValueOfPatternFromUTC("MM", utc);
        int day = getLocalValueOfPatternFromUTC("dd", utc);
        int hours = getLocalValueOfPatternFromUTC("HH", utc);
        int minutes = getLocalValueOfPatternFromUTC("mm", utc);
        int seconds = getLocalValueOfPatternFromUTC("ss", utc);
        return new DateTime(year, month, day, hours, minutes, seconds);
    }

    public static long convertLocalToUTC(DateTime local) {
        String s = String.format("%s%s.%s%s.%s %s%s:%s%s:%s%s",
                local.day / 10, local.day % 10, local.month / 10, local.month % 10,
                local.year, local.hours / 10, local.hours % 10,
                local.minutes / 10, local.minutes % 10, local.seconds / 10, local.seconds % 10);
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault()).parse(s, Instant::from).getEpochSecond();
    }

    public static int getLengthOfMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }

    public static long addDaysToUTC(long utc, int days) {
        return utc + 86400L * days;
    }

    public static long getMonthStartUTCFromLocal(int year, int month) {
        return DateTime.convertLocalToUTC(new DateTime(year, month, 1, 0, 0, 0));
    }

    public static long getMonthEndUTCFromLocal(int year, int month) {
        month++;
        if (month == 13) {
            month = 1;
            year++;
        }
        return getMonthStartUTCFromLocal(year, month);
    }

    public static String printDateTime(Context context, DateTime value) {
        return (printDate(context, value) + " " + printTime(context, value)).trim();
    }

    public static String printDate(Context context, DateTime value) {
        ConfigurationPreferences.FormatPreferences preferences = ConfigurationPreferences.getFormatPreferences(context);
        switch (preferences.formatDate) {
            default:
            case DD_MM_YYYY:
                return String.format("%s%s.%s%s.%s", value.day / 10, value.day % 10, value.month / 10, value.month % 10, value.year);
            case MM_DD_YYYY:
                return String.format("%s%s.%s%s.%s", value.month / 10, value.month % 10, value.day / 10, value.day % 10, value.year);
            case YYYY_MM_DD:
                return String.format("%s.%s%s.%s%s", value.year, value.month / 10, value.month % 10, value.day / 10, value.day % 10);
        }
    }

    public static String printYearMonth(Context context, int year, int month){
        ConfigurationPreferences.FormatPreferences preferences = ConfigurationPreferences.getFormatPreferences(context);
        switch (preferences.formatDate) {
            default:
            case DD_MM_YYYY:
            case MM_DD_YYYY:
                return String.format("%s%s.%s", month / 10, month % 10, year);
            case YYYY_MM_DD:
                return String.format("%s.%s%s", year, month / 10, month % 10);
        }
    }

    public static String printTime(Context context, DateTime value) {
        ConfigurationPreferences.FormatPreferences preferences = ConfigurationPreferences.getFormatPreferences(context);
        switch (preferences.formatTime) {
            default:
            case HH_MM:
                return String.format("%s%s:%s%s", value.hours / 10, value.hours % 10, value.minutes / 10, value.minutes % 10);
            case HH_MM_SS:
                return String.format("%s%s:%s%s:%s%s", value.hours / 10, value.hours % 10, value.minutes / 10, value.minutes % 10, value.seconds / 10, value.seconds % 10);
            case None:
                return "";
        }
    }
}
