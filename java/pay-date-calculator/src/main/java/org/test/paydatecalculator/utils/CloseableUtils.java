package org.test.paydatecalculator.utils;

import java.io.Closeable;
import java.io.IOException;

public class CloseableUtils {

	private CloseableUtils() {
		// TODO Auto-generated constructor stub
	}

	public static void safeClose(final Closeable toClose) {

		if (toClose == null) {
			return;
		}
		try {
			toClose.close();
		} catch (final IOException ioe) {

		}
	}
}
