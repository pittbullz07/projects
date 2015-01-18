package org.test.paydatecalculator.common;

import java.util.Arrays;
import java.util.Date;

import org.test.paydatecalculator.exception.CalculatorException;

/**
 * Base class used to perform Date computations. This class is abstract so it
 * needs to be extended in order to be used. Extending classes need to override
 * the {{@link #calculateDates()} method.
 *
 * @author Florin Matei
 *
 */
public abstract class DateCalculator {

	private final Date startDate;
	private final DateWriter[] dateWriters;

	public DateCalculator(final Date startDate, final DateWriter[] dateWriters) {
		this.startDate = startDate == null ? new Date() : startDate;
		this.dateWriters = dateWriters;
	}

	/**
	 * Method which performs the computation and writes the result to the
	 * provided DateWriters.
	 *
	 * @throws CalculatorException
	 */
	public abstract void calculateDates() throws CalculatorException;

	/**
	 *
	 * @return a copy of the Date object which was used to initialize the
	 *         object.
	 */
	public Date getStartDate() {
		return new Date(this.startDate.getTime());
	}

	/**
	 *
	 * @return The array of writers used to initialize the object.
	 */
	protected DateWriter[] getWriters() {
		final DateWriter[] writers = this.dateWriters == null ? null
				: Arrays.copyOf(this.dateWriters, this.dateWriters.length);
		return writers;
	}
}
