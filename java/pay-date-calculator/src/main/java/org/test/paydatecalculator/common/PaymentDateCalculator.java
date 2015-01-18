package org.test.paydatecalculator.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.test.paydatecalculator.exception.CalculatorException;
import org.test.paydatecalculator.utils.CloseableUtils;
import org.test.paydatecalculator.utils.DateUtil;

/**
 * Class which implements the main Date calculation algorithm. If the provided
 * dates are greater than the maximum days in a month then it will replace the
 * supplied date with the last day of the month. If the provided dates are
 * before the current date then:<br>
 * 1. if both dates are out of range the month will be skipped 2. if just one of
 * the dates is out of range its fields will be empty
 *
 * If the computed Date is out of range (not in the current month) then:<br>
 * 1. if both computed dates are out of range the month will be skipped <br>
 * 2. if just one date is out of range then its fields will be empty.
 *
 * @author Florin Matei
 *
 */
public class PaymentDateCalculator extends DateCalculator {

	private final int salaryDay;
	private final int bonusDay;
	/**
	 * DateDifferentiator to compute the days needed before a weekend day. Used
	 * to compute salary pay days.
	 */
	private final DateDifferentiator daysBeforeWeekend = new DateDifferentiator() {

		@Override
		public int computeDateDifference(final int weekendDay,
				final int daysInWeek) {
			final int diff = Calendar.FRIDAY - weekendDay;

			return diff > 0 ? diff - daysInWeek : diff;
		}
	};

	/**
	 * DateDifferentiator used to compute the days needed after a weekend day.
	 * Used to compute the bonusDay.
	 */
	private final DateDifferentiator daysAfterWeekend = new DateDifferentiator() {

		@Override
		public int computeDateDifference(final int weekendDay,
				final int daysInWeek) {
			final int diff = Calendar.WEDNESDAY - weekendDay;

			return diff > 0 ? diff : daysInWeek + diff;
		}
	};

	/**
	 * PaymentDateCalculator constructor. If the salaryDay or the bonusDay are
	 * not valid month days (between 1 and 31) an
	 * {@link IllegalArgumentException} will be thrown.
	 *
	 * @param startDate
	 * @param dateWriters
	 * @param salaryDay
	 * @param bonusDay
	 */
	public PaymentDateCalculator(final Date startDate,
			final DateWriter[] dateWriters,
			final int salaryDay,
			final int bonusDay) {
		super(startDate, dateWriters);
		final int maxDay = Calendar.getInstance().getMaximum(Calendar.DAY_OF_MONTH);
		if ((salaryDay < 1) || (salaryDay > maxDay)) {
			throw new IllegalArgumentException("Salary day needs to be between 1 and "
					+ maxDay + "but it was " + salaryDay);
		}

		if ((bonusDay < 1) || (bonusDay > maxDay)) {
			throw new IllegalArgumentException("Bonus day needs to be between 1 and "
					+ maxDay + "but it was " + salaryDay);
		}
		this.salaryDay = salaryDay;
		this.bonusDay = bonusDay;
	}

	public PaymentDateCalculator(final DateWriter[] dateWriters,
			final int salaryDay,
			final int bonusDay) {
		this(null, dateWriters, salaryDay, bonusDay);
	}

	@Override
	public void calculateDates() throws CalculatorException {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(super.getStartDate());
		final List<PaymentMonth> list = new ArrayList<PaymentMonth>();
		this.calculateDate(calendar, calendar.get(Calendar.YEAR) + 1, list);

		final DateWriter[] writers = super.getWriters();
		if (writers != null) {
			for (final DateWriter writer : writers) {
				try {
					PaymentDateCalculator.printList(writer, list);
				} catch (final IOException ioe) {
					throw new CalculatorException("Could not write result", "", ioe);
				} finally {
					CloseableUtils.safeClose(writer);
				}
			}
		}
	}

	private static void printList(final DateWriter writer,
			final List<PaymentMonth> list) throws IOException {
		for (final PaymentMonth month : list) {
			writer.write(month);
		}
	}

	private void calculateDate(final Calendar calendar, final int endYear,
			final List<PaymentMonth> list) {

		if (calendar.get(Calendar.YEAR) >= endYear) {
			return;
		}

		final int thisMonth = calendar.get(Calendar.MONTH);

		final Date salaryDate = PaymentDateCalculator.getPaymentDate(DateUtil.copyCalendar(calendar), this.salaryDay, this.daysBeforeWeekend);
		final Date bonusDate = PaymentDateCalculator.getPaymentDate(DateUtil.copyCalendar(calendar), this.bonusDay, this.daysAfterWeekend);

		calendar.set(Calendar.MONTH, thisMonth);
		if ((salaryDate != null) || (bonusDate != null)) {
			final String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
			list.add(new PaymentMonth(monthName, salaryDate, bonusDate));
		}

		// The actual date is only important for the first date since it may be
		// after the current date.
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1);

		this.calculateDate(calendar, endYear, list);
	}

	/**
	 * Calculates the payment date. If the payDay is a weekend it uses the
	 * Differentiators to get the first valid date. If there payDay is not valid
	 * and no valid pay day can be found it will return null.
	 *
	 * @param calendar
	 * @param payDay
	 * @param differentiator
	 * @return
	 */
	private static Date getPaymentDate(final Calendar calendar,
			final int payDay, final DateDifferentiator differentiator) {
		final int maximumDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		final int daysInAWeek = calendar.getActualMaximum(Calendar.DAY_OF_WEEK);
		final int today = calendar.get(Calendar.DAY_OF_MONTH);
		final int thisMonth = calendar.get(Calendar.MONTH);

		if (today > payDay) {
			return null;
		}
		calendar.set(Calendar.DAY_OF_MONTH, payDay < maximumDaysInMonth ? payDay
				: maximumDaysInMonth);

		final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

		if (DateUtil.isWeekend(dayOfWeek)) {
			calendar.add(Calendar.DAY_OF_MONTH, differentiator.computeDateDifference(dayOfWeek, daysInAWeek));
		}
		if (thisMonth == calendar.get(Calendar.MONTH)) {
			return calendar.getTime();
		}
		return null;
	}

}
