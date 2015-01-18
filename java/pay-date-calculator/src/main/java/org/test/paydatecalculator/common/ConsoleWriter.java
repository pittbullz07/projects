package org.test.paydatecalculator.common;

import java.io.IOException;
import java.text.DateFormat;

/**
 * Basic implementation of the {@link DateWriter} interface which prints to
 * stdout
 *
 * @author Florin Matei
 *
 */
public class ConsoleWriter implements DateWriter {
	private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);

	@Override
	public void close() throws IOException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void write(final PaymentMonth toWrite) {
		System.out.print(this.getFormattedString(toWrite));
	}

	@Override
	public void setDateFormat(final DateFormat dateFormat) {
		if (dateFormat != null) {
			this.dateFormat = dateFormat;
		}
	}

	private String getFormattedString(final PaymentMonth month) {
		final String salaryDateString = month.hasSalaryDate() ? this.dateFormat.format(month.getSalaryDate())
				: "";
		final String bonusDateString = month.hasBonusDate() ? this.dateFormat.format(month.getBonusDate())
				: "";
		final String line = month.getMonthName() + " | " + salaryDateString
				+ " | " + bonusDateString + System.lineSeparator();

		return line;
	}
}
