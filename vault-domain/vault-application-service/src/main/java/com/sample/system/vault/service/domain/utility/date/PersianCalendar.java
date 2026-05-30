package com.sample.system.vault.service.domain.utility.date;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class PersianCalendar extends Calendar {

	private static final String[] FIELD_NAME = { "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND",
			"ZONE_OFFSET", "DST_OFFSET" };
	// Month Constants:

	/**
	 *
	 */
	public static final int FARVARDIN = 1;

	/**
	 *
	 */
	public static final int ORDIBEHESHT = 2;

	/**
	 *
	 */
	public static final int KHORDAD = 3;

	/**
	 *
	 */
	public static final int TIR = 4;

	/**
	 *
	 */
	public static final int MORDAD = 5;

	/**
	 *
	 */
	public static final int SHAHRIVAR = 6;

	/**
	 *
	 */
	public static final int MEHR = 7;

	/**
	 *
	 */
	public static final int ABAN = 8;

	/**
	 *
	 */
	public static final int AZAR = 9;

	/**
	 *
	 */
	public static final int DEY = 10;

	/**
	 *
	 */
	public static final int BAHMAN = 11;

	/**
	 *
	 */
	public static final int ESFAND = 12;

	/**
	 * Converts the current field values in fields[] to the millisecond time value
	 * time
	 */
	protected void computeTime() {
		GregorianCalendar gc;
		int j_y;
		int j_m;
		int j_d;

		int[] g_days_in_month = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		int[] j_days_in_month = { 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29 };
		int jy;
		int jm;
		int jd;
		int j_day_no;
		int g_day_no;
		int gy;
		int gm;
		int gd;
		boolean leap;
		int i;

		j_d = fields[DAY_OF_MONTH];
		j_m = fields[MONTH] + 1;
		j_y = fields[YEAR];

		if (j_d < 0 || j_d > getMaximum(DAY_OF_MONTH)) {
			throw new IllegalArgumentException(FIELD_NAME[DAY_OF_MONTH]);
		}

		if (j_m < 1 || j_m > 12) {
			throw new IllegalArgumentException(FIELD_NAME[MONTH]);
		}

		jy = j_y - 979;
		jm = j_m - 1;
		jd = j_d - 1;

		j_day_no = 365 * jy + div(jy, 33) * 8 + div(jy % 33 + 3, 4);
		for (i = 0; i < jm; i++) {
			j_day_no += j_days_in_month[i];
		}

		j_day_no += jd;

		g_day_no = j_day_no + 79;

		gy = 1600 + 400 * div(g_day_no, 146097); /* 146097 = 365*400 + 400/4 - 400/100 + 400/400 */
		g_day_no = g_day_no % 146097;

		leap = true;
		if (g_day_no >= 36525) {
			g_day_no--;
			gy += 100 * div(g_day_no, 36524); /* 36524 = 365*100 + 100/4 - 100/100 */
			g_day_no = g_day_no % 36524;

			if (g_day_no >= 365) {
				g_day_no++;
			} else {
				leap = false;
			}
		}

		gy += 4 * div(g_day_no, 1461); /* 1461 = 365*4 + 4/4 */
		g_day_no %= 1461;

		if (g_day_no >= 366) {
			leap = false;

			g_day_no--;
			gy += div(g_day_no, 365);
			g_day_no = g_day_no % 365;
		}

		for (i = 0; g_day_no >= (g_days_in_month[i] + ((i == 1 && leap) ? 1 : 0)); i++) {
			g_day_no -= (g_days_in_month[i] + ((i == 1 && leap) ? 1 : 0));
		}

		gm = i + 1;
		gd = g_day_no + 1;

		gc = new GregorianCalendar(gy, gm - 1, gd, fields[HOUR_OF_DAY], fields[MINUTE], fields[SECOND]);
		time = gc.getTime().getTime();
	}

	/**
	 * Converts the current millisecond time value time to calendar field values in
	 * fields[]. This allows you to sync up the calendar field values with a new
	 * time that is set for the calendar. The time is not recomputed first; to
	 * recompute the time, then the fields, call the complete() method.
	 */

	protected void computeFields() {
		int g_days_in_month[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		int j_days_in_month[] = { 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29 };
		GregorianCalendar gc;
		int g_y;
		int g_m;
		int g_d;
		int gy;
		int gm;
		int gd;
		int g_day_no;
		int i;
		int j_day_no;
		int j_np;
		int jy;
		int jm;
		int jd;

		gc = new GregorianCalendar();

		gc.setTime(new Date(time));

		g_d = gc.get(GregorianCalendar.DAY_OF_MONTH);
		g_y = gc.get(GregorianCalendar.YEAR);
		g_m = gc.get(GregorianCalendar.MONTH) + 1;

		gy = g_y - 1600;
		gm = g_m - 1;
		gd = g_d - 1;

		g_day_no = 365 * gy + div(gy + 3, 4) - div(gy + 99, 100) + div(gy + 399, 400);

		for (i = 0; i < gm; i++) {
			g_day_no += g_days_in_month[i];
		}

		if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
			/* leap and after Feb */
			g_day_no++;
		}

		g_day_no += gd;

		j_day_no = g_day_no - 79;

		j_np = div(j_day_no, 12053); /* 12053 = 365*33 + 32/4 */
		j_day_no = j_day_no % 12053;

		jy = 979 + 33 * j_np + 4 * div(j_day_no, 1461); /* 1461 = 365*4 + 4/4 */

		j_day_no %= 1461;

		if (j_day_no >= 366) {
			jy += div(j_day_no - 1, 365);
			j_day_no = (j_day_no - 1) % 365;
		}

		for (i = 0; i < 11 && j_day_no >= j_days_in_month[i]; ++i) {
			j_day_no -= j_days_in_month[i];
		}
		jm = i + 1;
		jd = j_day_no + 1;

		fields[DAY_OF_MONTH] = jd;
		fields[MONTH] = jm - 1;
		fields[YEAR] = jy;
		fields[DAY_OF_WEEK] = gc.get(DAY_OF_WEEK);
		fields[HOUR] = gc.get(HOUR);
		fields[HOUR_OF_DAY] = gc.get(HOUR_OF_DAY);
		fields[MINUTE] = gc.get(MINUTE);
		fields[SECOND] = gc.get(SECOND);
	}

	/**
	 *
	 * @see
	 */
	public void add(int field, int amount) {
	}

	/**
	 *
	 * @see <code>java.util.Calendar</code> abstract class
	 */
	public void roll(int field, boolean up) {
	}

	/**
	 *
	 * @see <code>java.util.Calendar</code> abstract class
	 */
	public int getMinimum(int field) {
		GregorianCalendar gc;

		gc = new GregorianCalendar();
		gc.setTime(getTime());

		return gc.getMinimum(field);
	}

	/**
	 *
	 * @see <code>java.util.Calendar</code> abstract class
	 */
	public int getMaximum(int field) {
		Calendar c;

		if (field == DAY_OF_MONTH) {

			if (fields[MONTH] < (MEHR - 1)) {
				return 31;
			}

			if (fields[MONTH] < (ESFAND - 1)) {
				return 30;
			}

			return isLeapYear(fields[YEAR]) ? 30 : 29;
		}

		c = new GregorianCalendar();

		c.setTime(getTime());

		return c.getMaximum(field);
	}

	/**
	 *
	 * @see <code>java.util.Calendar</code> abstract class
	 */
	public int getGreatestMinimum(int field) {
		GregorianCalendar gc;

		gc = new GregorianCalendar();
		gc.setTime(getTime());

		return gc.getGreatestMinimum(field);
	}

	/**
	 *
	 * @see <code>java.util.Calendar</code> abstract class
	 */
	public int getLeastMaximum(int field) {
		GregorianCalendar gc;

		gc = new GregorianCalendar();
		gc.setTime(getTime());

		return gc.getLeastMaximum(field);
	}

	/**
	 *
	 * @return current year
	 */
	public static int getCurrentYear() {
		PersianCalendar calendar;

		calendar = new PersianCalendar();
		calendar.setTime(new Date());
		return calendar.get(YEAR);
	}

	protected static boolean isLeapYear(int year) {
		int d;
		int l;

		d = year - 1309;
		l = d / 33;
		l = 33 * l;
		l = l + 1309;
		return ((year - l) % 4) == 0;
	}

	public static int div(int a, int b) {
		return (a / b);
	}

}
