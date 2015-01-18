package org.test.paydatecalculator.tests.mock;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.test.paydatecalculator.common.DateWriter;
import org.test.paydatecalculator.common.PaymentMonth;

public class MockDateWriter implements DateWriter {

	private final List<PaymentMonth> months;

	public MockDateWriter() {
		this.months = new ArrayList<PaymentMonth>();
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public void setDateFormat(final DateFormat dateFormat) {

	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void write(final PaymentMonth toWrite) throws IOException {
		this.months.add(toWrite);
	}

	public List<PaymentMonth> getMonths() {
		return this.months;
	}
}
