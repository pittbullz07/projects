/**
 *
 */
package org.test.paydatecalculator.tests.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.test.paydatecalculator.common.PaymentMonth;

/**
 * @author Florin Matei
 *
 */
public class PaymentMonthTest {

	@Test
	public void testIllegalArguments() {
		final Calendar instance = Calendar.getInstance();
		instance.set(2015, Calendar.JANUARY, 15);
		final Date d1 = instance.getTime();
		instance.set(2015, Calendar.JANUARY, 20);
		final Date d2 = instance.getTime();
		try {
			new PaymentMonth(null, d1, d2);
			fail("null month name should not be possible");
		} catch (final IllegalArgumentException arg) {

		}

		try {
			new PaymentMonth("a", null, null);
			fail("Both dates shouldn't be null");
		} catch (final IllegalArgumentException arg) {
		}

		final PaymentMonth paymentMonth = new PaymentMonth("a", d1, null);

		assertFalse(paymentMonth.hasBonusDate());
		assertTrue(paymentMonth.hasSalaryDate());
		assertNull(paymentMonth.getBonusDate());
		assertNotNull(paymentMonth.getSalaryDate());

		final PaymentMonth paymentMonth2 = new PaymentMonth("a", null, d2);

		assertTrue(paymentMonth2.hasBonusDate());
		assertFalse(paymentMonth2.hasSalaryDate());
		assertNotNull(paymentMonth2.getBonusDate());
		assertNull(paymentMonth2.getSalaryDate());
	}

	@Test
	public void testEquals() {

		final Calendar instance = Calendar.getInstance();
		instance.set(2015, Calendar.JANUARY, 15, 10, 24);
		instance.getTime();
		instance.getTime();

		instance.set(2015, Calendar.JANUARY, 15, 11, 04);
		final Date month1Bonus = instance.getTime();
		final Date month2Bonus = instance.getTime();
		instance.set(2015, Calendar.JANUARY, 16, 11, 04);
		final Date d3 = instance.getTime();

		final PaymentMonth pm1 = new PaymentMonth("a", month1Bonus, month1Bonus);
		final PaymentMonth pm2 = new PaymentMonth("a", month2Bonus, month2Bonus);
		final PaymentMonth pm3 = new PaymentMonth("b", month2Bonus, month2Bonus);
		final PaymentMonth pm4 = new PaymentMonth("b", d3, month1Bonus);
		final PaymentMonth pm5 = new PaymentMonth("b", month1Bonus, d3);

		assertTrue("Payment months should be equal if monthName and the the year, month and date are equal", pm1.equals(pm2));
		assertFalse("Payment months should be equal if monthName and the the year, month and date are equal", pm1.equals(pm3));
		assertFalse("Payment months should be equal if monthName and the the year, month and date are equal", pm1.equals(pm4));
		assertFalse("Payment months should be equal if monthName and the the year, month and date are equal", pm1.equals(pm5));
	}
}
