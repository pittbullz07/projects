package org.test.paydatecalculator.common;

/**
 * Interface which defines an operation performed when a date is out of bounds
 *
 * @author Florin Matei
 *
 */
public interface DateDifferentiator {
	/**
	 * The implementation of this method should compute the number of days
	 * forward or backward to get from the input week day to the desired week
	 * day. <br>
	 * If the desired date will be before then the return value needs to be
	 * negative while if it is positive then the desired date will be after the
	 * weekendDay.
	 *
	 * @param weekendDay
	 * @param daysInWeek
	 * @return
	 */
	public int computeDateDifference(final int weekendDay, final int daysInWeek);
}