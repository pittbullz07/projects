package org.test.paydatecalculator.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of the {@link DateWriter} interface which handles writing
 * results to a file.
 *
 * @author Florin Matei
 *
 */
public class DateFileWriter implements DateWriter {

	private final String fileName;
	private final File outputFile;
	private final BufferedWriter writer;
	private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);

	/**
	 * If the file provided as input doesn't exist then it will be created. If
	 * it exists and can be written to then it will be overwritten. If the file
	 * can't be written to then an {@link IOException} will be thrown.
	 *
	 * If no name is specified then a default name will be created for the file
	 * which is the current calendar date (DD_MM_YYYY.csv).
	 *
	 * @param filePath
	 * @throws IOException
	 */
	public DateFileWriter(final String filePath) throws IOException {
		this.fileName = DateFileWriter.getFileName(filePath);
		this.outputFile = new File(this.fileName);

		if (this.outputFile.exists()) {
			if (!this.outputFile.canWrite()) {
				throw new IOException("No permissions to access file: "
						+ this.fileName);
			}
		} else {
			final boolean createdNewFile = this.outputFile.createNewFile();
			if (!createdNewFile) {
				throw new IOException("Could not create file " + this.fileName);
			}
		}
		this.writer = new BufferedWriter(new FileWriter(this.outputFile));
	}

	@Override
	public void close() throws IOException {
		this.writer.close();

	}

	@Override
	public void flush() throws IOException {
		this.writer.flush();
	}

	@Override
	public void write(final PaymentMonth toWrite) throws IOException {
		this.writer.write(this.getFormattedString(toWrite));

	}

	private String getFormattedString(final PaymentMonth month) {
		final String salaryDateString = month.hasSalaryDate() ? this.dateFormat.format(month.getSalaryDate())
				: "";
		final String bonusDateString = month.hasBonusDate() ? this.dateFormat.format(month.getBonusDate())
				: "";
		final String line = month.getMonthName() + ", " + salaryDateString
				+ ", " + bonusDateString + System.lineSeparator();

		return line;
	}

	private static String getFileName(final String filePath) {
		final String name;
		if ((filePath == null) || filePath.isEmpty()) {
			final DateFormat d = new SimpleDateFormat("dd_MM_yyyy.'csv'");
			name = d.format(new Date());
		} else {
			name = filePath;
		}
		return name;
	}

	@Override
	public void setDateFormat(final DateFormat dateFormat) {
		if (dateFormat != null) {
			this.dateFormat = dateFormat;
		}
	}

}
