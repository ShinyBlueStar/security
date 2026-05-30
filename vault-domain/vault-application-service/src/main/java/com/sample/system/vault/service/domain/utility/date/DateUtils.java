package com.sample.system.vault.service.domain.utility.date;

import com.sample.system.vault.service.domain.utility.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class DateUtils {
    /**
     *
     */
    public static final Logger log = LogManager.getLogger(DateUtils.class);

    /**
     *
     */
    public static final String ENGLISH_LANGUAGE = "en";

    /**
     *
     */
    public static final String PERSIAN_DATE_FORMAT = "yyyy/MM/dd";

    /**
     *
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";


    /**
     *
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";


    /**
     *
     */
    public static final String FARSI_LANGUAGE = "fa";

    /**
     *
     */
    public static final Locale FARSI_LOCALE = new Locale(FARSI_LANGUAGE);

    /**
     *
     */
    public static final Locale ENGLISH_LOCALE = new Locale(ENGLISH_LANGUAGE);

    public static Date formatDate(Date start, String format, Locale locale) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        String date1 = simpleDateFormat.format(start);
        return DateUtils.parse(date1, format, true, locale);
    }

    /**
     * @param date
     * @return a Date
     */
    public static Date roundDate(Date date) {
        GregorianCalendar calendar;

        calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(GregorianCalendar.HOUR, 0);
        calendar.set(GregorianCalendar.MINUTE, 0);
        calendar.set(GregorianCalendar.SECOND, 0);
        calendar.set(GregorianCalendar.MILLISECOND, 0);
        calendar.set(GregorianCalendar.AM_PM, GregorianCalendar.AM);

        return calendar.getTime();
    }

    /**
     * <p>
     * Checks if the field is a valid date. The pattern is used with
     * <code>java.text.SimpleDateFormat</code>. If strict is true, then the length
     * will be checked so '2/12/1999' will not pass validation with the format
     * 'MM/dd/yyyy' because the month isn't two digits. The setLenient method is set
     * to <code>false</code> for all.
     * </p>
     *
     * @param value       The value validation is being performed on.
     * @param datePattern The pattern passed to <code>SimpleDateFormat</code>.
     * @param strict      Whether or not to have an exact match of the datePattern.
     */
    public static Date parse(String value, String datePattern, boolean strict, Locale locale) {
        Date date;
        DateFormat formatter;

        if (value == null || datePattern == null || datePattern.length() == 0) {
            return null;
        }

        try {

            formatter = null;

            if (locale != null) {
                if (FARSI_LOCALE.getLanguage().equals(locale.getLanguage())) {
                    formatter = new PersianDateFormat(datePattern);
                }
            }

            if (formatter == null) {
                formatter = new SimpleDateFormat(datePattern);
            }

            formatter.setLenient(false);

            date = formatter.parse(value);

            if (strict) {
                if (datePattern.length() != value.length()) {
                    date = null;
                }
            }
        } catch (ParseException ex) {
            log.error(ex.getMessage());
            return null;
        }

        return date;
    }

    /**
     * <p>
     * Checks if the field is a valid date. The <code>Locale</code> is used with
     * <code>java.text.DateFormat</code>. The setLenient method is set to
     * <code>false</code> for all.
     * </p>
     *
     * @param value  The value validation is being performed on.
     * @param locale The Locale to use to parse the date (system default if null)
     */
    public static Date parse(String value, Locale locale) {
        Date date;
        DateFormat formatter;

        if (value == null) {
            return null;
        }

        try {

            formatter = null;
            if (locale != null) {
                if (FARSI_LOCALE.getLanguage().equals(locale.getLanguage())) {
                    formatter = new PersianDateFormat(PERSIAN_DATE_FORMAT);
                }
            }

            if (formatter == null) {
                formatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            }

            formatter.setLenient(false);

            date = formatter.parse(value);
        } catch (ParseException ex) {
            log.warn(value, ex);
            return null;
        }

        return date;
    }

    /**
     * @param locale
     * @param date
     * @param format
     * @param translate
     * @return a String
     */
    public static String getLocaleDate(Locale locale, Date date, String format, boolean translate) {
        if (date == null) {
            return null;
        }
        DateFormat dateFormat;

        if (locale != null && FARSI_LOCALE.getLanguage().equals(locale.getLanguage())) {
            dateFormat = new PersianDateFormat((format == null) ? PERSIAN_DATE_FORMAT : format);
        } else {
            dateFormat = new SimpleDateFormat((format == null) ? DEFAULT_DATE_FORMAT : format, Locale.getDefault());
        }

        String result = translate ? StringUtils.getLocaleText(locale, dateFormat.format(date)) : dateFormat.format(date);

        if (locale != null && FARSI_LOCALE.getLanguage().equals(locale.getLanguage())) {
            if (format != null && !format.startsWith("yyyy") && format.startsWith("yy")) {
                result = result.substring(2);
            }
        }

        return result;

    }

    /**
     * returns the difference between two dates in day scale
     *
     * @param fromDate
     * @param untilDate
     * @return an int value
     */
    public static int getDifference(Date fromDate, Date untilDate) {
        long difference;

        difference = (untilDate.getTime() - fromDate.getTime());

        return (int) Math.ceil(difference / (1000 * 60 * 60 * 24));
    }

    /**
     * returns the date time of a day before given current date
     *
     * @param currentDate
     * @return a Date object
     */
    public static Date getPreviousDay(Date currentDate) {
        GregorianCalendar calendar;
        int day;

        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.DAY_OF_MONTH, day - 1);

        return calendar.getTime();
    }

    /**
     * returns the date time of a day before given current date
     *
     * @param currentDate
     * @return a Date object
     */
    public static Date getNextDay(Date currentDate) {

        GregorianCalendar calendar;
        int day;

        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.DAY_OF_MONTH, day + 1);

        return calendar.getTime();
    }

    /**
     * returns the date time of a day before given current date
     *
     * @param currentDate
     * @return a Date object
     */
    public static Date getNextMonth(Date currentDate) {

        GregorianCalendar calendar;
        int month;

        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        month = calendar.get(Calendar.MONTH);

        calendar.set(Calendar.MONTH, month + 1);

        return calendar.getTime();
    }

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static Date convertToDateViaInstant(LocalDate dateToConvert) {
        if (dateToConvert == null) {
            return null;
        }
        return Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * checks whether given date is between start date and end date or not
     *
     * @param date
     * @param startDate
     * @param endDate
     * @return true if given date occurred between startDate and endDate otherwise
     * false
     */
    public static boolean checkInterval(Date date, Date startDate, Date endDate) {

        if (startDate != null) {
            if (date.compareTo(startDate) < 0) {
                return false;
            }
        }

        if (endDate != null) {
            if (date.compareTo(endDate) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * provides a Date Time range started from beging of specified day date
     * 00:00:00.000 until the next day 00:00:00.000
     *
     * @param dayDate
     * @return a Date array
     */
    public static Date[] getDayInterval(Date dayDate) {
        GregorianCalendar calendar;
        Date[] range;

        range = new Date[2];
        calendar = new GregorianCalendar();
        calendar.setTime(dayDate);

        calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
        calendar.set(GregorianCalendar.MINUTE, 0);
        calendar.set(GregorianCalendar.SECOND, 0);
        calendar.set(GregorianCalendar.MILLISECOND, 0);

        range[0] = calendar.getTime();

        calendar.set(GregorianCalendar.DAY_OF_MONTH, calendar.get(GregorianCalendar.DAY_OF_MONTH) + 1);

        range[1] = calendar.getTime();

        return range;
    }

    /**
     * provides a Date Time range started from beging of specified month date
     * 00:00:00.000 until the month date 00:00:00.000
     *
     * @param dayDate the current day date.
     * @param locale  ;
     * @return a Date array
     */
    public static Date[] getMonthDateInterval(Date dayDate, Locale locale) {
        Calendar calendar;
        Date[] range;
        int month;

        range = new Date[2];

        if (FARSI_LANGUAGE.equals(locale.getLanguage())) {
            calendar = new PersianCalendar();
        } else {
            calendar = new GregorianCalendar();
        }

        calendar.setTime(dayDate);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        range[0] = calendar.getTime();

        month = calendar.get(GregorianCalendar.MONTH);

        if (month < calendar.getMaximum(Calendar.MONTH)) {
            calendar.set(Calendar.MONTH, month + 1);
        } else {
            calendar.set(Calendar.MONTH, calendar.getMinimum(Calendar.MONTH));
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        }

        range[1] = calendar.getTime();

        return range;
    }

    /**
     * creates a calendar based on given date and given locale.
     *
     * @param date
     * @param locale
     * @return a Calendar initate by given date.
     */
    public static Calendar getCalendar(Date date, Locale locale) {
        Calendar calendar;
        if (locale != null && FARSI_LANGUAGE.equals(locale.getLanguage())) {
            calendar = new PersianCalendar();
        } else {
            calendar = new GregorianCalendar();
        }

        calendar.setTime(date);

        return calendar;
    }

    /**
     * converts year, month and day to a Date based on specified Locale.
     *
     * @param year
     * @param month
     * @param day
     * @param locale
     * @return a Date object
     */
    public static Date getDate(int year, int month, int day, Locale locale) {
        Calendar calendar;

        if (locale != null && FARSI_LANGUAGE.equals(locale.getLanguage())) {
            calendar = new PersianCalendar();
        } else {
            calendar = new GregorianCalendar();
        }

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        return calendar.getTime();
    }

    /**
     * returns the n * day before given current date
     *
     * @param currentDate
     * @param n
     * @return a Date object
     */
    public static Date getNPreviousDay(Date currentDate, int n) {
        GregorianCalendar calendar;
        int day;

        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        day = calendar.get(Calendar.DAY_OF_YEAR);

        calendar.set(Calendar.DAY_OF_YEAR, day - n);

        return calendar.getTime();
    }

    public static long getDifferenceInMinutes(Date startDate, Date endDate) {
        return ChronoUnit.MINUTES.between(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * Calculates the difference in minutes between a Date and a timestamp.
     *
     * @param date      The Date object
     * @param timestamp The timestamp in milliseconds (long)
     * @return The absolute difference in minutes
     */
    public static long getDifferenceInMinutes(Date date, long timestamp) {
        // Get the timestamp in milliseconds from the Date object
        long dateMillis = date.getTime();

        // Calculate the absolute difference in milliseconds
        long diffMillis = Math.abs(dateMillis - timestamp);

        // Convert the difference to minutes
        return TimeUnit.MILLISECONDS.toMinutes(diffMillis);
    }

    /**
     * returns the n * day before given current date
     *
     * @param currentDate
     * @param n
     * @return a Date object
     */
    public static Date getNPreviousMinutes(Date currentDate, int n) {
        GregorianCalendar calendar;
        int min;

        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        min = calendar.get(Calendar.MINUTE);

        calendar.set(Calendar.MINUTE, min - n);

        return calendar.getTime();
    }

    public static Date getNNextDay(String currentDateStr, int n) {

        GregorianCalendar calendar;
        int day;
        calendar = new GregorianCalendar();
        Date currentDate = parse(currentDateStr, "yyyyMMddHHmm", true, DateUtils.ENGLISH_LOCALE);
        calendar.setTime(currentDate);
        day = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.set(Calendar.DAY_OF_YEAR, day + n);

        return calendar.getTime();
    }

    public static Date getNNextDayReturnDate(Date currentDate, int n) {

        GregorianCalendar calendar;
        int day;
        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        day = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.set(Calendar.DAY_OF_YEAR, day + n);
        return calendar.getTime();
    }

    public static String getNNextDay(Date currentDate, int n) {

        GregorianCalendar calendar;
        int day;
        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        day = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.set(Calendar.DAY_OF_YEAR, day + n);

        return getLocaleDate(DateUtils.ENGLISH_LOCALE, calendar.getTime(), "yyyyMMddHHmm", false);
    }


    public static String getNNextDayWithFormat(Date currentDate, int n, String format) {

        GregorianCalendar calendar;
        int day;
        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        day = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.set(Calendar.DAY_OF_YEAR, day + n);

        return getLocaleDate(DateUtils.ENGLISH_LOCALE, calendar.getTime(), format, false);
    }

    public static long getDifferenceInDay(Date startDate, Date endDate) {
        return ChronoUnit.DAYS.between(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public static String getFirstDateOfNextMonth(String lastDate) {

        int year = Integer.parseInt(lastDate.substring(0, 4));
        int month = Integer.parseInt(lastDate.substring(4, 6));

        month = month + 1;

        if (month > 12) {
            month = 1;
            year = year + 1;
        }

        Date firstDateOfNextMonth = DateUtils.getDate(year, month, 1, DateUtils.FARSI_LOCALE);

        return DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, firstDateOfNextMonth, "yyyy-MM-dd", false);

    }

    public static String getLastDateOfNextMonth(String lastDate) {

        int year = Integer.parseInt(lastDate.substring(0, 4));
        int month = Integer.parseInt(lastDate.substring(4, 6));
        int day = 30;

        month = month + 1;

        if (month > 12) {
            month = 1;
            year = year + 1;
        }

        if (month <= 6) {
            day = 31;
        } else if (month > 6 && month < 12) {
            day = 30;
        } else if (month == 12) {
            day = 29;
        }

        Date lastDateOfNextMonth = DateUtils.getDate(year, month, day, DateUtils.FARSI_LOCALE);

        return DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, lastDateOfNextMonth, "yyyy-MM-dd", false);

    }

    public static String getFirstDateOfpreviousMonth() {

        String currentDateStr = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, new Date(), "yyyyMM", false);

        int year = Integer.parseInt(currentDateStr.substring(0, 4));
        int month = Integer.parseInt(currentDateStr.substring(4, 6));

        month = month - 1;

        if (month <= 0) {
            month = 12;
            year = year - 1;
        }

        return DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, DateUtils.getDate(year, month, 1, DateUtils.FARSI_LOCALE), "yyyyMMdd", false);

    }

    public static String getlastDateOfpreviousMonth() {

        String currentDateStr = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, new Date(), "yyyyMM", false);

        int year = Integer.parseInt(currentDateStr.substring(0, 4));
        int month = Integer.parseInt(currentDateStr.substring(4, 6));
        int day = 30;

        month = month - 1;

        if (month <= 0) {
            month = 12;
            year = year - 1;
        }

        if (month <= 6) {
            day = 31;
        } else if (month > 6 && month < 12) {
            day = 30;
        } else if (month == 12) {

            if (PersianCalendar.isLeapYear(year)) {
                day = 30;
            } else {
                day = 29;
            }

        }

        return DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, DateUtils.getDate(year, month, day, DateUtils.FARSI_LOCALE), "yyyyMMdd", false);

    }

    public static Date getLastDateOfMonth(String currentDateStr) {

        int year = Integer.parseInt(currentDateStr.substring(0, 4));
        int month = Integer.parseInt(currentDateStr.substring(4, 6));
        int day = 30;

        if (month <= 6) {
            day = 31;
        } else if (month > 6 && month < 12) {
            day = 30;
        } else if (month == 12) {
            if (PersianCalendar.isLeapYear(year)) {
                day = 30;
            } else {
                day = 29;
            }
        }

        Date lastDateOfMonth = DateUtils.getDate(year, month, day, DateUtils.FARSI_LOCALE);
        return lastDateOfMonth;
    }

    public static Date getFirstDateOfMonth(String currentDateStr) {

        int year = Integer.parseInt(currentDateStr.substring(0, 4));
        int month = Integer.parseInt(currentDateStr.substring(4, 6));
        int day = 1;

        Date firstDateOfMonth = DateUtils.getDate(year, month, day, DateUtils.FARSI_LOCALE);
        return firstDateOfMonth;
    }

    public static String getNextMonth(String currentMonth) { // 139701

        int lastMonthOfCalculation = Integer.valueOf(currentMonth.substring(4, 6));
        int lastYearOfCalculation = Integer.valueOf(currentMonth.substring(0, 4));

        if (lastMonthOfCalculation + 1 > 12) {
            lastYearOfCalculation++;
            lastMonthOfCalculation = 1;
        } else {
            lastMonthOfCalculation++;
        }

        String nextmonth = String.valueOf(lastYearOfCalculation) + org.apache.commons.lang3.StringUtils.leftPad(String.valueOf(lastMonthOfCalculation), 2, '0'); // 139702

        return nextmonth;

    }

    public static String getPreviousMonth(String currentMonth) { // 139702

        int lastMonthOfCalculation = Integer.valueOf(currentMonth.substring(4, 6));
        int lastYearOfCalculation = Integer.valueOf(currentMonth.substring(0, 4));

        if (lastMonthOfCalculation - 1 <= 0) {
            lastYearOfCalculation--;
            lastMonthOfCalculation = 12;
        } else {
            lastMonthOfCalculation--;
        }

        String previousmonth = String.valueOf(lastYearOfCalculation) + org.apache.commons.lang3.StringUtils.leftPad(String.valueOf(lastMonthOfCalculation), 2, '0'); // 139701

        return previousmonth;

    }

    public static Date getNNextMinutes(Date currentDate, int n) {

        GregorianCalendar calendar;
        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        int minutes = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE, minutes + n);
        return calendar.getTime();
    }

    public static Date getNNextSecond(Date currentDate, int n) {

        GregorianCalendar calendar;
        calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        int second = calendar.get(Calendar.SECOND);
        calendar.set(Calendar.SECOND, second + n);
        return calendar.getTime();
    }

    public static String geyDayOfWeek(Date date, Locale locale) {
        DateFormat formatter = new SimpleDateFormat("EEEE", locale);
        return formatter.format(date);
    }

    public static String geyDayOfWeek(LocalDate date, Locale locale) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
    }

    public static String gregorianFromJalali(String jalaliDate) {
        return gregorianFromJalali(jalaliDate, PERSIAN_DATE_FORMAT);
    }

    public static String gregorianFromJalali(String jalaliDate, String datePattern) {
        try {
            Date gregorianDate = parse(jalaliDate, datePattern, false, FARSI_LOCALE);
            SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
            return formatter.format(gregorianDate);
        } catch (Exception exception) {
            log.error("exception in converting jalali to gregorian, message ({})", exception.getMessage());
            return null;
        }
    }

    public static Date parseGregorian(String date) {
        if (StringUtils.hasText(date)) {
            Date gregorianDate;
            if (Integer.parseInt(date.substring(0, Math.min(4, date.length() - 1))) < 1500) {
                gregorianDate = DateUtils.parse(date, DateUtils.DEFAULT_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                gregorianDate = DateUtils.parse(date, DateUtils.DEFAULT_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            return gregorianDate;
        }
        return null;
    }

    public static Long timestampFromJalali(String jalaliDate) {
        return timestampFromJalali(jalaliDate, PERSIAN_DATE_FORMAT);
    }

    public static Long timestampFromJalali(String jalaliDate, String pattern) {
        if (!StringUtils.hasText(jalaliDate)) {
            return null;
        }


        try {
            Date gregorianDate = parse(jalaliDate, pattern, false, FARSI_LOCALE);
            return gregorianDate.getTime();
        } catch (Exception exception) {
            log.error("exception in converting jalali to timestamp, message ({})", exception.getMessage());
            return null;
        }
    }

    public static Integer getAge(Date birthDate) {
        LocalDate birthLocalDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birthLocalDate, LocalDate.now()).getYears();
    }

    public static void main(String[] args) {

        String currentDate = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, new Date(), "yyyyMMdd", false);
        String lastDateOfpreviousMonth = DateUtils.getlastDateOfpreviousMonth();
        String firstDateOfpreviousMonth = DateUtils.getFirstDateOfpreviousMonth();

        System.out.println(lastDateOfpreviousMonth);
        System.out.println(firstDateOfpreviousMonth);

        DateFormat formatter = new SimpleDateFormat("F", DateUtils.FARSI_LOCALE);
        System.out.println(formatter.format(new Date()).toString());
        LocalDate today = LocalDate.now();

        DayOfWeek dayOfWeek = today.getDayOfWeek();
        System.out.println("" + dayOfWeek.getValue());

        System.out.println(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, new Date(Long.parseLong("1736533973110")), "yyyyMMdd", false));

        // System.out.println(DateUtils.getFirstDateOfMonth("139712"));
        // System.out.println(DateUtils.getLastDateOfMonth("139712"));
        //
        // System.out.println(DateUtils.getFirstDateOfMonth("139710"));
        // System.out.println(DateUtils.getLastDateOfMonth("139710"));
        //
        // System.out.println(DateUtils.getPreviousMonth("139710"));
        // System.out.println(DateUtils.getPreviousMonth("139701"));

    }
}
