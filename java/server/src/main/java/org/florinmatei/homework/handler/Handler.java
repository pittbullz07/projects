/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.handler;

import org.florinmatei.homework.server.BasicServer;

/**
 * Base class for handlers. For now it's mostly a marker class so we can provide
 * consistency when registering handlers. All handlers need to extend this
 * class.
 *
 * In the future more functionality might be added.
 *
 * @author Florin Matei
 */
public abstract class Handler {

    protected final BasicServer server;

    public Handler(final BasicServer server) {
        this.server = server;
    }
}
