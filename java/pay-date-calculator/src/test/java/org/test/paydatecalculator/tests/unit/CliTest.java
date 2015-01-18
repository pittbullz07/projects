package org.test.paydatecalculator.tests.unit;

import org.junit.Assert;
import org.junit.Test;
import org.test.paydatecalculator.Cli;
import org.test.paydatecalculator.exception.CalculatorException;

/**
 *
 * @author Florin Matei
 *
 */
public class CliTest {

	private static final int DEFAULT_INT_VALUE = 1;
	private static final int TEST_SALARY_VALUE = 55;
	private static final int TEST_BONUS_VALUE = 67;
	private static final String TEST_FILE_NAME = "testtest123";

	/**
	 * Test that the Cli processing works as expected. Try invalid arguments
	 *
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		final String[] empty = {};
		Cli cli = new Cli(empty);

		// 1. Make sure the default retrieval works when the options are empty
		Assert.assertEquals(DEFAULT_INT_VALUE, cli.getAsInteger(Cli.OPTION_SALARY_DAY, DEFAULT_INT_VALUE));
		Assert.assertEquals(DEFAULT_INT_VALUE, cli.getAsInteger(Cli.OPTION_BONUS_DAY, DEFAULT_INT_VALUE));
		Assert.assertNull(cli.getAsString(Cli.OPTION_FILE_NAME));
		Assert.assertFalse(cli.optionExists(Cli.OPTION_HELP));
		Assert.assertFalse(cli.optionExists(Cli.OPTION_HELP_LONG));

		final String[] help = { "-" + Cli.OPTION_HELP };
		cli = new Cli(help);
		Assert.assertTrue(cli.optionExists(Cli.OPTION_HELP));
		Assert.assertTrue(cli.optionExists(Cli.OPTION_HELP_LONG));

		final String[] helpLong = { "--" + Cli.OPTION_HELP_LONG };
		cli = new Cli(helpLong);
		Assert.assertTrue(cli.optionExists(Cli.OPTION_HELP));
		Assert.assertTrue(cli.optionExists(Cli.OPTION_HELP_LONG));

		// 2. Make sure the retrieval fails when providing invalid input
		// arguments
		final String[] wrong = { "-" + Cli.OPTION_SALARY_DAY, " -!34" };
		cli = new Cli(wrong);
		try {
			cli.getAsInteger(Cli.OPTION_SALARY_DAY, DEFAULT_INT_VALUE);
			Assert.fail("Illegal value should cause an exception");
		} catch (final CalculatorException ce) {
		}

		final String[] wrong2 = { "-" + Cli.OPTION_BONUS_DAY, "-!34" };
		cli = new Cli(wrong2);
		try {
			cli.getAsInteger(Cli.OPTION_BONUS_DAY, DEFAULT_INT_VALUE);
			Assert.fail("Illegal value should cause an exception");
		} catch (final CalculatorException ce) {
		}

		// 3. Make sure processing of arguments fails when the option is unknown
		try {
			final String[] unknown = { "-abcd43" };
			cli = new Cli(unknown);
			Assert.fail("Illegal value should cause an exception");
		} catch (final CalculatorException ce) {
		}

		// 4. Test that retrieving valid input options works
		final String[] good = { "-" + Cli.OPTION_SALARY_DAY,
				String.valueOf(TEST_SALARY_VALUE), "-" + Cli.OPTION_BONUS_DAY,
				String.valueOf(TEST_BONUS_VALUE), "-" + Cli.OPTION_FILE_NAME,
				TEST_FILE_NAME };
		cli = new Cli(good);

		Assert.assertEquals(TEST_SALARY_VALUE, cli.getAsInteger(Cli.OPTION_SALARY_DAY, DEFAULT_INT_VALUE));
		Assert.assertEquals(TEST_BONUS_VALUE, cli.getAsInteger(Cli.OPTION_BONUS_DAY, DEFAULT_INT_VALUE));
		Assert.assertNotNull(cli.getAsString(Cli.OPTION_FILE_NAME));
		Assert.assertEquals(TEST_FILE_NAME, cli.getAsString(Cli.OPTION_FILE_NAME));
	}

}
