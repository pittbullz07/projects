/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server;

import java.io.IOException;
import java.net.Socket;
import org.florinmatei.homework.server.exceptions.ResponseException;

/**
 *
 * @author Florin Matei
 */
public interface Session {

    public Request getRequest();

    public Response getResponse();

    public Socket getSocket();

    public void next() throws IOException, ResponseException;

    public void sendResponse() throws Exception;
}
