package muzdima.mymoney.utils;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTime {
    public static long getNowUTC() {
        return Instant.now().getEpochSecond();
    }

    public static int getCurrentYear() {
        return Integer.parseInt(DateTimeFormatter.ofPattern("yyyy").withZone(ZoneId.from(ZoneOffset.UTC)).format(Instant.now()));
    }

    public static int getCurrentMonth() {
        return Integer.parseInt(DateTimeFormatter.ofPattern("MM").withZone(ZoneId.from(ZoneOffset.UTC)).format(Instant.now()));
    }

    public static long getMonthStartUTC(int year, int month) {
        return Instant.parse(String.format("%s-%s%s-01T00:00:00Z",year,month/10,month%10)).getEpochSecond();
    }

    public static long getMonthEndUTC(int year, int month) {
        month++;
        if (month == 13){
            month = 1;
            year++;
        }
        return getMonthStartUTC(year, month);
    }

    public static long getCurrentMonthStartUTC() {
        return getMonthStartUTC(getCurrentYear(), getCurrentMonth());
    }

    public static long getCurrentMonthEndUTC() {
        return getMonthEndUTC(getCurrentYear(), getCurrentMonth());
    }

    public static int getLengthOfMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }

    private static String printUTCToLocal(long utc, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault()).format(Instant.ofEpochSecond(utc));
    }

    public static String printUTCToLocal(long utc) {
        return printUTCToLocal(utc, "dd.MM.yyyy\nHH:mm");
    }

    public static String printUTCToLocalDate(long utc) {
        return printUTCToLocal(utc, "dd.MM.yyyy");
    }

    public static String printUTCToLocalTime(long utc) {
        return printUTCToLocal(utc, "HH:mm");
    }

    public static String printLocalDate(int year, int month, int dayOfMonth) {
        return String.format("%s%s.%s%s.%s%s%s%s", dayOfMonth / 10, dayOfMonth % 10, month / 10, month % 10, year / 1000, year / 100 % 10, year / 10 % 10, year % 10);
    }

    public static String printLocalTime(int hourOfDay, int minute) {
        return String.format("%s%s:%s%s", hourOfDay / 10, hourOfDay % 10, minute / 10, minute % 10);
    }

    public static long parseUTCFromLocal(String date, String time) {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault()).parse(String.format("%s %s", date, time), Instant::from).getEpochSecond();
    }

    public static int getLocalYear(long utc) {
        return Integer.parseInt(printUTCToLocal(utc, "yyyy"));
    }

    public static int getLocalMonth(long utc) {
        return Integer.parseInt(printUTCToLocal(utc, "MM"));
    }

    public static int getLocalDayOfMonth(long utc) {
        return Integer.parseInt(printUTCToLocal(utc, "dd"));
    }

    public static int getLocalHourOfDay(long utc) {
        return Integer.parseInt(printUTCToLocal(utc, "HH"));
    }

    public static int getLocalMinute(long utc) {
        return Integer.parseInt(printUTCToLocal(utc, "mm"));
    }

    public static long getLocalDayStartUTC(long utc){
        return parseUTCFromLocal(DateTime.printUTCToLocalDate(utc), "00:00");
    }

    public static long addDays(long utc, int days){
        return utc + 86400L * days;
    }
}
