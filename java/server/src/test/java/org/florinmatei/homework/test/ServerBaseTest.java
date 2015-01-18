/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.impl.client.DefaultHttpClient;
import org.florinmatei.homework.server.BasicServer;
import org.florinmatei.homework.utils.CloseableUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 *
 * @author Florin Matei
 */
public class ServerBaseTest {

    protected final Logger LOG = Logger.getLogger(BasicServer.class.getName());
    protected BasicServer server;
    protected DefaultHttpClient httpclient;

    @Before
    public void setUp() throws IOException {
        final File propertiesFile = new File("server.properties");

        final Properties properties = new Properties();
        final FileInputStream fileInputStream = new FileInputStream(propertiesFile);
        try {
            properties.load(fileInputStream);
        }
        catch (Exception e) {

        }
        finally {
            CloseableUtils.safeClose(fileInputStream);
        }
        server = new MockServer(properties);
        httpclient = new DefaultHttpClient();
        try {
            server.start();
        }
        catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        Assert.assertTrue(server.serverStarted());
    }

    @After
    public void tearDown() {
        server.stop();
        httpclient.getConnectionManager().shutdown();
    }
}
