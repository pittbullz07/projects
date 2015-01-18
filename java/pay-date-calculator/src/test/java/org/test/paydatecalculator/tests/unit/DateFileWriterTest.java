package org.test.paydatecalculator.tests.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Test;
import org.test.paydatecalculator.common.DateFileWriter;
import org.test.paydatecalculator.common.PaymentMonth;

public class DateFileWriterTest {
	private static final String TEST_LINE = "test";
	private static final String FILE_NAME = "test";
	private final PaymentMonth month = new PaymentMonth("a", new Date(), new Date());
	private final DateFormat dateInstance = DateFormat.getDateInstance();

	@After
	public void tearDown() {
		new File(FILE_NAME).delete();
	}

	/**
	 * Test the {@link DateFileWriter} to see if it works.<br>
	 * 1. Create a new file and mark it as not writable. Make sure
	 * {@link DateFileWriter} fails at initialization <br>
	 * 2. Test if the {@link DateFileWriter} falls back to the current date file
	 * naming <br>
	 * 3. Make sure that the {@link DateFileWriter} overwrites an exising
	 * writable file.
	 *
	 * @throws IOException
	 */
	@Test
	public void existingFileTest() throws IOException {
		// 1. Create a new file and mark it as not writable. Make sure {@link
		// DateFileWriter} fails at initialization
		final File notWritableFile = new File(FILE_NAME);
		notWritableFile.createNewFile();
		notWritableFile.setWritable(false);

		try {
			new DateFileWriter(FILE_NAME);
			fail("DateFileWriter should fail on locked files");
		} catch (final IOException ioe) {
		} finally {
			notWritableFile.delete();
		}

		// 2. Test if the {@link DateFileWriter} falls back to the current date
		// file naming
		final DateFormat d = new SimpleDateFormat("dd_MM_yyyy.'csv'");
		final String dateFileName = d.format(new Date());
		File dateNameFile = null;
		try (final DateFileWriter dateFileWriter = new DateFileWriter(null)) {

			dateFileWriter.write(this.month);
			dateFileWriter.flush();
			dateNameFile = new File(dateFileName);
			assertTrue(dateNameFile.exists());
			assertTrue(dateNameFile.length() > 0);
		} catch (final IOException ioe) {

		} finally {
			if (dateNameFile != null) {
				dateNameFile.delete();
			}
		}

		// 3. Make sure that the {@link DateFileWriter} overwrites an exising
		// writable file.
		final File testFile = new File(FILE_NAME);
		testFile.createNewFile();
		try (final BufferedReader reader = new BufferedReader(new FileReader(testFile));
				final FileWriter fw = new FileWriter(testFile)) {
			fw.write(TEST_LINE);
			fw.flush();

			final String readLine = reader.readLine();
			assertEquals(TEST_LINE, readLine);
			assertNull(reader.readLine());
		}

		try (final BufferedReader reader = new BufferedReader(new FileReader(testFile));
				final DateFileWriter dfw = new DateFileWriter(FILE_NAME)) {

			dfw.setDateFormat(this.dateInstance);
			final String expected = this.month.getMonthName() + ", "
					+ this.dateInstance.format(this.month.getSalaryDate())
					+ ", "
					+ this.dateInstance.format(this.month.getSalaryDate());
			dfw.write(this.month);
			dfw.flush();

			final String newLine = reader.readLine();
			assertFalse(TEST_LINE.equals(newLine));
			assertEquals(expected, newLine);
			assertNull(reader.readLine());
		}
	}
}
