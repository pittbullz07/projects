/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server;

import java.net.Socket;

/**
 *
 * @author Florin Matei
 */
public interface SessionFactory extends ServiceProvider {

    public Session createSession(final Socket socket);
}
