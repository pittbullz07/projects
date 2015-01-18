/**
 *
 */
package org.test.paydatecalculator.tests.unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.test.paydatecalculator.common.DateWriter;
import org.test.paydatecalculator.common.PaymentDateCalculator;
import org.test.paydatecalculator.common.PaymentMonth;
import org.test.paydatecalculator.exception.CalculatorException;
import org.test.paydatecalculator.tests.mock.MockDateWriter;
import org.test.paydatecalculator.utils.DateUtil;

/**
 * Tests for verifying the functionality of the {@link PaymentDateCalculator}
 *
 * @author Florin Matei
 *
 */
public class PaymentDateCalculatorTest {

	private final static int NO_PAY_DAY = -1;

	/**
	 * Test out of bounds values for the salary and bonus days. In all cases
	 * {@link IllegalArgumentException} should be thrown
	 *
	 * @throws IOException
	 */
	@Test
	public void testInvalidDates() throws IOException {
		final Date date = new Date();
		final DateWriter[] emptyWriters = {};
		try {
			new PaymentDateCalculator(date, emptyWriters, NO_PAY_DAY, 10);
			Assert.fail("Incorrect salary date should fail");
		} catch (final IllegalArgumentException iae) {
		}

		try {
			new PaymentDateCalculator(date, emptyWriters, 100, 10);
			Assert.fail("Incorrect salary date should fail");
		} catch (final IllegalArgumentException iae) {
		}

		try {
			new PaymentDateCalculator(date, emptyWriters, 1, NO_PAY_DAY);
			Assert.fail("Incorrect salary date should fail");
		} catch (final IllegalArgumentException iae) {
		}

		try {
			new PaymentDateCalculator(date, emptyWriters, 1, 100);
			Assert.fail("Incorrect salary date should fail");
		} catch (final IllegalArgumentException iae) {
		}
	}

	/**
	 * Used the {@link PaymentDateCalculator} to calculate salary dates for 2015
	 * and verify them against a hand created list.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCorrectDates2015() throws Exception {
		// set the date initial date after the bonus day to make sure it is
		// skipped
		final List<PaymentMonth> expectedMonths = PaymentDateCalculatorTest.getExpectedList2015();
		final List<PaymentMonth> actualMonths = PaymentDateCalculatorTest.calculateFor(2015, Calendar.JANUARY, 16, 31, 15);

		Assert.assertEquals("Actual number of months is different from the expected one", expectedMonths.size(), actualMonths.size());

		for (int i = 0; i < expectedMonths.size(); i++) {
			Assert.assertEquals("The expected month is not equal to the computed one", expectedMonths.get(i), actualMonths.get(i));
		}
	}

	/**
	 * Used the {@link PaymentDateCalculator} to calculate salary dates for 2016
	 * (which is a leap year) and verify them against a hand created list.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCorrectDates2016() throws Exception {

		final List<PaymentMonth> expectedMonths = PaymentDateCalculatorTest.getExpectedList2016();
		final List<PaymentMonth> actualMonths = PaymentDateCalculatorTest.calculateFor(2016, Calendar.JANUARY, 1, 31, 15);

		Assert.assertEquals("Actual number of months is different from the expected one", expectedMonths.size(), actualMonths.size());

		for (int i = 0; i < expectedMonths.size(); i++) {
			Assert.assertEquals("The expected month is not equal to the computed one", expectedMonths.get(i), actualMonths.get(i));
		}
	}

	/**
	 * Test that the {@link PaymentDateCalculator} handles properly limit cases
	 * of the dates. Since the salary will be paid before the salaryDay if the
	 * day is a weekend day we set the salary day to the 1st. Since the bonuses
	 * are paid after the bonus day if it's a weekend then we set it to the 31st
	 *
	 * @throws Exception
	 */
	@Test
	public void testLimitDates() throws Exception {
		final List<PaymentMonth> expectedMonths = PaymentDateCalculatorTest.getExpectedLimitList();

		final List<PaymentMonth> actualMonths = PaymentDateCalculatorTest.calculateFor(2015, Calendar.JANUARY, 1, 1, 31);

		Assert.assertEquals("Actual number of months is different from the expected one", expectedMonths.size(), actualMonths.size());

		for (int i = 0; i < expectedMonths.size(); i++) {
			Assert.assertEquals("The expected month is not equal to the computed one", expectedMonths.get(i), actualMonths.get(i));
		}
	}

	/**
	 * Creates a new {@link PaymentDateCalculator} using the provided
	 * information uses it to calculate the {@link PaymentMonth} objects and
	 * returns the result in a list
	 *
	 * @param year
	 *            the year of the initial date
	 * @param month
	 *            the month of the initial date
	 * @param day
	 *            the day of the initial date
	 * @param salaryDay
	 *            the salary day which is used in the computations
	 * @param bonusDay
	 *            the bonus day which is used in the computations
	 * @return
	 * @throws CalculatorException
	 */
	private static List<PaymentMonth> calculateFor(final int year,
			final int month, final int day, final int salaryDay,
			final int bonusDay) throws Exception {
		try (final MockDateWriter mockDateWriter = new MockDateWriter()) {
			final DateWriter[] mock = { mockDateWriter };

			final Calendar instance = Calendar.getInstance();
			instance.set(year, month, day);
			final PaymentDateCalculator paymentDateCalculator = new PaymentDateCalculator(instance.getTime(), mock, salaryDay, bonusDay);
			paymentDateCalculator.calculateDates();
			return mockDateWriter.getMonths();
		}
	}

	/**
	 * Creates a list with the values for the limit test case
	 *
	 * @return
	 */
	private static List<PaymentMonth> getExpectedLimitList() {
		final List<PaymentMonth> months = new ArrayList<PaymentMonth>();
		final Calendar calendar = Calendar.getInstance();
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.JANUARY, 1, NO_PAY_DAY));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.MARCH, NO_PAY_DAY, 31));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.APRIL, 1, 30));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.MAY, 1, NO_PAY_DAY));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.JUNE, 1, 30));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.JULY, 1, 31));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.AUGUST, NO_PAY_DAY, 31));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.SEPTEMBER, 1, 30));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.OCTOBER, 1, NO_PAY_DAY));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.NOVEMBER, NO_PAY_DAY, 30));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.DECEMBER, 1, 31));
		return months;
	}

	/**
	 * Creates a list with the expected payment dates for:<br>
	 * - year: 2016<br>
	 * - salary day: 31st<br>
	 * - bonus day: 15th
	 *
	 * @return
	 */
	private static List<PaymentMonth> getExpectedList2016() {
		final List<PaymentMonth> months = new ArrayList<PaymentMonth>();
		final Calendar calendar = Calendar.getInstance();
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.JANUARY, 29, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.FEBRUARY, 29, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.MARCH, 31, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.APRIL, 29, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.MAY, 31, 18));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.JUNE, 30, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.JULY, 29, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.AUGUST, 31, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.SEPTEMBER, 30, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.OCTOBER, 31, 19));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.NOVEMBER, 30, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2016, Calendar.DECEMBER, 30, 15));
		return months;
	}

	/**
	 * Creates a list with the expected payment dates for:<br>
	 * - year: 2015<br>
	 * - salary day: 31st<br>
	 * - bonus day: 15th
	 *
	 * @return
	 */
	private static List<PaymentMonth> getExpectedList2015() {
		final List<PaymentMonth> months = new ArrayList<PaymentMonth>();
		final Calendar calendar = Calendar.getInstance();
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.JANUARY, 30, NO_PAY_DAY));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.FEBRUARY, 27, 18));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.MARCH, 31, 18));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.APRIL, 30, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.MAY, 29, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.JUNE, 30, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.JULY, 31, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.AUGUST, 31, 19));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.SEPTEMBER, 30, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.OCTOBER, 30, 15));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.NOVEMBER, 30, 18));
		PaymentDateCalculatorTest.populateList(months, DateUtil.buildPaymentMonth(calendar, 2015, Calendar.DECEMBER, 31, 15));
		return months;
	}

	/**
	 * Utility method which adds the month object to the list if the month is
	 * not null
	 *
	 * @param list
	 * @param month
	 */
	private static void populateList(final List<PaymentMonth> list,
			final PaymentMonth month) {
		if (month != null) {
			list.add(month);
		}
	}
}
