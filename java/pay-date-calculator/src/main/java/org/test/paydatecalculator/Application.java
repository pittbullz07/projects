/**
 *
 */
package org.test.paydatecalculator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.test.paydatecalculator.common.ConsoleWriter;
import org.test.paydatecalculator.common.DateCalculator;
import org.test.paydatecalculator.common.DateFileWriter;
import org.test.paydatecalculator.common.DateWriter;
import org.test.paydatecalculator.common.PaymentDateCalculator;
import org.test.paydatecalculator.exception.CalculatorException;

/**
 * @author Florin Matei
 *
 */
public class Application {
	public final static String APPLICATION_NAME = "paydatecalculator";
	private static final int DEFAULT_SALARY_DAY = 31;
	private static final int DEFAULT_BONUS_DAY = 15;

	private final static Logger LOG = Logger.getLogger("pay-date-calculator");

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		try {
			final Application sc = new Application();
			sc.calculate(args);
		} catch (final CalculatorException calculatorException) {
			System.out.println(calculatorException.getMessage());
			System.out.println(calculatorException.getHelpMessage());
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
			LOG.log(Level.SEVERE, null, ex);
		}
	}

	/**
	 *
	 * @param args
	 * @throws CalculatorException
	 */
	private void calculate(final String[] args) throws CalculatorException,
			IOException {
		final Cli cli = new Cli(args);
		if (cli.optionExists(Cli.OPTION_HELP)
				|| cli.optionExists(Cli.OPTION_HELP_LONG)) {
			System.out.println(cli.generateUsageHelpMessage());
			return;
		}

		final int salaryDay = cli.getAsInteger(Cli.OPTION_SALARY_DAY, DEFAULT_SALARY_DAY);
		final int bonusDay = cli.getAsInteger(Cli.OPTION_BONUS_DAY, DEFAULT_BONUS_DAY);
		final String fileName = cli.getAsString(Cli.OPTION_FILE_NAME);
		try (final DateWriter consoleWriter = new ConsoleWriter();
				final DateWriter fileWriter = new DateFileWriter(fileName)) {
			final DateWriter[] writers = { consoleWriter, fileWriter };
			final DateCalculator dc = new PaymentDateCalculator(writers, salaryDay, bonusDay);
			dc.calculateDates();
		}
	}
}
