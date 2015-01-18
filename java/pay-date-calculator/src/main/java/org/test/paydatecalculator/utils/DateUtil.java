package org.test.paydatecalculator.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.test.paydatecalculator.common.PaymentMonth;

public class DateUtil {
	private DateUtil() {
	}

	public static boolean isWeekend(final int day) {
		return (day == Calendar.SATURDAY) || (day == Calendar.SUNDAY);
	}

	public static Calendar copyCalendar(final Calendar calendar) {
		final Calendar instance = Calendar.getInstance();
		instance.setTime(calendar.getTime());
		return instance;
	}

	/**
	 * Returns true if the year, month and day of the two dates are equal (the
	 * hours are ignored).
	 *
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static boolean datesEqual(final Date d1, final Date d2) {
		if (d1 == d2) {
			return true;
		}

		if ((d1 == null) || (d2 == null)) {
			return false;
		}

		final Calendar c1 = Calendar.getInstance();
		final Calendar c2 = Calendar.getInstance();
		c1.setTime(d1);
		c2.setTime(d2);

		boolean equals = c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
		equals &= c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
		equals &= c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);

		return equals;
	}

	/**
	 * Creates a new {@link PaymentMonth} object using the supplied parameters.
	 * If the salaryDay or the bonusDay parameters are NO_PAY_DAY then the
	 * respective dates will be set to null. The final salaryDay and bonusDay
	 * Date objects will have the supplied year and month
	 *
	 * @param calendar
	 * @param year
	 * @param month
	 * @param salaryDay
	 * @param bonusDay
	 * @return
	 */
	public static PaymentMonth buildPaymentMonth(final Calendar calendar,
			final int year, final int month, final int salaryDay,
			final int bonusDay) {
		calendar.set(year, month, 1);
		final Date salaryDate;
		if (salaryDay > 0) {
			calendar.set(Calendar.DAY_OF_MONTH, salaryDay);
			salaryDate = calendar.getTime();
		} else {
			salaryDate = null;
		}
		final Date bonusDate;
		if (bonusDay > 0) {
			calendar.set(Calendar.DAY_OF_MONTH, bonusDay);
			bonusDate = calendar.getTime();
		} else {
			bonusDate = null;
		}
		final String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
		final boolean hasDate = (salaryDate != null) || (bonusDate != null);
		return hasDate ? new PaymentMonth(monthName, salaryDate, bonusDate)
				: null;
	}
}
