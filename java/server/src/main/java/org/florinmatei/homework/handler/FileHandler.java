/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.handler;

import org.florinmatei.homework.server.BasicServer;
import org.florinmatei.homework.server.Session;

/**
 * Marker class for FileHandler. Any handler which is going to receive or send
 * files needs to extend class. There will only be 1 FileHandler per server
 * which will serve requests not handled by other handlers as a "last resort".
 *
 * Context handlers can take of file themselves if they want to or they can pass
 * it to the FileHandler.
 *
 * @author Florin Matei
 */
public abstract class FileHandler extends Handler {

    public FileHandler(final BasicServer server) {
        super(server);
    }

    /**
     * General handler which needs to handle all HTTP operations for files.
     *
     * @param session
     * @throws Exception
     */
    public abstract void handle(final Session session) throws Exception;
}
