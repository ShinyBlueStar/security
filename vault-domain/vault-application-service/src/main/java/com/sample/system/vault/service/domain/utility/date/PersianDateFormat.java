package com.sample.system.vault.service.domain.utility.date;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PersianDateFormat extends SimpleDateFormat {

	static class PersianDateFormatSymbols extends DateFormatSymbols {
		static final String[] months = { "\u0641\u0631\u0648\u0631\u062f\u064a\u0646", "\u0627\u0631\u062f\u064a\u0628\u0647\u0634\u062a", "\u062e\u0631\u062f\u0627\u062f", "\u062a\u064a\u0631", "\u0645\u0631\u062f\u0627\u062f",
				"\u0634\u0647\u0631\u064a\u0648\u0631", "\u0645\u0647\u0631", "\u0622\u0628\u0627\u0646", "\u0622\u0630\u0631", "\u062f\u064a", "\u0628\u0647\u0645\u0646", "\u0627\u0633\u0641\u0646\u062f" };

		static final String[] amPmString = { "\u0642.\u0638", // AM
				"\u0628. \u0638" // PM
		};

		static final String[] eras = { "\u0628.\u0647", "\u0642.\u0647" };

		static String[] aweekdays = { "", "\u064a\u06a9\u0634\u0646\u0628\u0647", "\u062f\u0648\u0634\u0646\u0628\u0647", "\u0633\u0647 \u0634\u0646\u0628\u0647", "\u0686\u0647\u0627\u0631\u0634\u0646\u0628\u0647",
				"\u067e\u0646\u062c \u0634\u0646\u0628\u0647", "\u062c\u0645\u0639\u0647", "\u0634\u0646\u0628\u0647" };

		public PersianDateFormatSymbols() {
			init();
		}

		public PersianDateFormatSymbols(Locale locale) {
			super(locale);
			init();
		}

		protected void init() {
			setMonths(months);
			setShortMonths(months);
			setAmPmStrings(amPmString);
			setEras(eras);
			setWeekdays(aweekdays);
		}

	}

	/**
	 *
	 * @param pattern the date/time pattern
	 */
	public PersianDateFormat(String pattern) {
		super(pattern, new PersianDateFormatSymbols());
		initialize();
	}

	/**
	 * initialize the calendar 2 digit year start
	 */
	protected void initialize() {
		Calendar calendar;
		setCalendar(calendar = new com.sample.system.vault.service.domain.utility.date.PersianCalendar());
		calendar.clear();
		calendar.set(Calendar.YEAR, (com.sample.system.vault.service.domain.utility.date.PersianCalendar.getCurrentYear() / 100) * 100);
		set2DigitYearStart(calendar.getTime());
	}

}
