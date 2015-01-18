package org.test.paydatecalculator.common;

import java.io.Closeable;
import java.io.IOException;
import java.text.DateFormat;

/**
 * Interface which needs to be implemented in order to be able to receive
 * processing results from {@link PaymentDateCalculator} objects.
 *
 * @author Florin Matei
 *
 */
public interface DateWriter extends Closeable {

	/**
	 * Set the DateFormat used to format the dates when writing the
	 * {@link PaymentMonth} objects
	 *
	 * @param dateFormat
	 */
	public void setDateFormat(final DateFormat dateFormat);

	public void flush() throws IOException;

	/**
	 *
	 * @param toWrite
	 * @throws IOException
	 */
	public void write(final PaymentMonth toWrite) throws IOException;
}
