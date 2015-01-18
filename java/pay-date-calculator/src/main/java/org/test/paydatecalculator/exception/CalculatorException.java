package org.test.paydatecalculator.exception;

public class CalculatorException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -460247106355129310L;
	private final String helpMessage;

	public CalculatorException(final String cause, final String message) {
		this(cause, message, null);
	}

	public CalculatorException(final String causeMessage,
			final String helpMessage,
			final Throwable cause) {
		super(causeMessage, cause);
		this.helpMessage = helpMessage;
	}

	public String getHelpMessage() {
		return this.helpMessage;
	}
}
