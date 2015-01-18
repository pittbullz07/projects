/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.utils;

import java.io.Closeable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.florinmatei.homework.server.BasicServer;

/**
 *
 * @author Florin Matei
 */
public final class CloseableUtils {

    private static final Logger LOG = Logger.getLogger(BasicServer.class.getName());

    private CloseableUtils() {
    }

    public static void safeClose(final Closeable... toClose) {
        if (toClose != null && toClose.length > 0) {
            for (final Closeable closeable : toClose) {
                safeClose(closeable);
            }
        }
    }

    public static void safeClose(final Closeable toClose) {
        if (toClose != null) {
            try {
                toClose.close();
            }
            catch (Exception closeException) {
                LOG.log(Level.WARNING, "Closing " + toClose.toString() + " failed!", closeException);
            }
        }
    }
}
