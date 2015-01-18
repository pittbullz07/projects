/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.test;

import java.util.Properties;
import org.florinmatei.homework.server.BasicServer;

/**
 *
 * @author Florin Matei
 */
public class MockServer extends BasicServer {

    public MockServer(Properties properties) {
        super(properties);
        super.registerHandler(new MockHandler(this));
    }

}
