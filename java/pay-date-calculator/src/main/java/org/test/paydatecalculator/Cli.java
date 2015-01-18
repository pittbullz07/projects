package org.test.paydatecalculator;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.test.paydatecalculator.exception.CalculatorException;

/**
 * Wrapper over Apache Common CLI classes. This class will process the program
 * arguments for the desired parameters. If any errors occur a
 * {@link CalculatorException} will be thrown
 *
 * @author Florin Matei
 *
 */
public class Cli {
	public static final String OPTION_SALARY_DAY = "s";
	public static final String OPTION_BONUS_DAY = "b";
	public static final String OPTION_FILE_NAME = "f";
	public static final String OPTION_HELP = "h";
	public static final String OPTION_HELP_LONG = "help";
	public static final String DESCRIPTION_SALARY_DAY = "The day in which the salary needs to be paid out (Ex. for the 24th this should be 24). If no value is specified 31 will be used as default";
	public static final String DESCRIPTION_BONUS_DAY = "The day in which the bonus needs to be paid out (Ex. for the 24th this should be 24). If not value is specified 15 will be used as default";
	public static final String DESCRIPTION_FILE_NAME = "The name of the file in which the dates will be saved. If no value is specified the file will be saved based on the current date as DD_MM_YYYY.csv";

	final Options options;
	final CommandLine commandLine;

	public Cli(final String[] args) throws CalculatorException {
		this.options = new Options();
		this.prepareCliOptions();
		this.commandLine = this.process(args);
	}

	/**
	 * Method used to get the value of an input parameter. The default value is
	 * used <b>only</b> if the value of the searched option is null or empty,
	 * otherwise the value will be processed. If the value is not null and
	 * processing fails a {@link CalculatorException} will be thrown
	 *
	 * @param option
	 * @param defaultValue
	 * @return
	 * @throws CalculatorException
	 */
	public int getAsInteger(final String option, final int defaultValue)
			throws CalculatorException {
		final String optionValue = this.commandLine.getOptionValue(option);
		int salaryDay = defaultValue;
		if ((optionValue != null) && !optionValue.isEmpty()) {
			try {
				salaryDay = Integer.parseInt(optionValue);
			} catch (final NumberFormatException nfe) {
				throw new CalculatorException("Invalid value for option -"
						+ option + ": " + optionValue, this.generateUsageHelpMessage());
			}
		}
		return salaryDay;
	}

	/**
	 * Returns the value of an option.
	 *
	 * @param option
	 * @return
	 */
	public String getAsString(final String option) {
		return this.commandLine.getOptionValue(option);
	}

	public boolean optionExists(final String option) {
		return this.commandLine.hasOption(option);
	}

	/**
	 *
	 * @return a formatted String containing the description of how to use the
	 *         CLI options.
	 */
	public String generateUsageHelpMessage() {
		final HelpFormatter formatter = new HelpFormatter();
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, Application.APPLICATION_NAME, null, this.options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null, true);
		pw.flush();
		pw.close();
		return sw.getBuffer().toString();
	}

	private Options prepareCliOptions() {
		final Option salaryDayOption = new Option(OPTION_SALARY_DAY, true, DESCRIPTION_SALARY_DAY);
		final Option bonusDayOption = new Option(OPTION_BONUS_DAY, true, DESCRIPTION_BONUS_DAY);
		final Option fileName = new Option(OPTION_FILE_NAME, true, DESCRIPTION_FILE_NAME);
		final Option help = new Option(OPTION_HELP, OPTION_HELP_LONG, false, "print this message");
		this.options.addOption(salaryDayOption);
		this.options.addOption(bonusDayOption);
		this.options.addOption(fileName);
		this.options.addOption(help);
		return this.options;
	}

	/**
	 *
	 * @param args
	 * @return
	 * @throws CalculatorException
	 */
	private CommandLine process(final String[] args) throws CalculatorException {
		final CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(this.options, args);
		} catch (final ParseException parseException) {
			throw new CalculatorException(parseException.getMessage(), this.generateUsageHelpMessage(), parseException);
		}

		return commandLine;
	}
}
