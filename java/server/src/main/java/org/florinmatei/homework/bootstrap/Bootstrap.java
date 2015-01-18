/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.florinmatei.homework.common.PropertyKeys;
import org.florinmatei.homework.server.BasicServer;
import org.florinmatei.homework.utils.CloseableUtils;

/**
 *
 * @author Florin Matei
 */
public final class Bootstrap {

    private static final String PROPERTY_FILE = "server.properties";
    private final Logger LOG = Logger.getLogger(Bootstrap.class.getName());
    private final Properties properties = new Properties();

    public static void main(final String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.start();
    }

    private Bootstrap() {

    }

    private BasicServer boot() {
        BasicServer server = null;
        final File propertiesFile = new File(PROPERTY_FILE);

        if (propertiesFile.exists()) {
            try {
                final FileInputStream fileInputStream = new FileInputStream(propertiesFile);
                try {
                    properties.load(fileInputStream);
                }
                catch (Exception e) {

                }
                finally {
                    CloseableUtils.safeClose(fileInputStream);
                }
            }
            catch (IOException ex) {
                LOG.log(Level.SEVERE, "Could not read properties");
            }
            String serverClassName = properties.getProperty(PropertyKeys.PROPERTY_SERVER_INSTANCE);
            if (serverClassName != null && !serverClassName.isEmpty()) {
                server = createServerInstance(serverClassName);

                if (server == null) {
                    LOG.log(Level.SEVERE, "No server instance created. Shutting down!");
                }
            } else {
                LOG.log(Level.SEVERE, "No server instance defined. Shutting down!");
            }
        } else {
            LOG.log(Level.SEVERE, "No properties file found. Shutting down!");
        }
        return server;
    }

    protected void start() {
        BasicServer server = boot();
        if (server == null) {
            System.exit(1);
        }
        try {
            server.start();
            server.getThread().join();
        }
        catch (IOException ioe) {
            LOG.log(Level.SEVERE, "", ioe);
        }
        catch (InterruptedException iex) {
            LOG.log(Level.SEVERE, "", iex);
        }
    }

    private BasicServer createServerInstance(final String serverClass) {
        BasicServer instance = null;
        try {
            final Class<BasicServer> loadClass = (Class<BasicServer>) Thread.currentThread().getContextClassLoader().loadClass(serverClass);
            instance = loadClass.getConstructor(Properties.class).newInstance(properties);
        }
        catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "Could not find Server class! Shutting down!", ex);
        }
        catch (InstantiationException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        catch (NoSuchMethodException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        catch (SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        catch (IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        catch (InvocationTargetException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return instance;
    }
}
