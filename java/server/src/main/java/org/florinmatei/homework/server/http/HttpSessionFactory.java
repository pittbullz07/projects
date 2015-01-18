/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server.http;

import java.net.Socket;
import org.florinmatei.homework.server.BasicServer;
import org.florinmatei.homework.server.HttpParser;
import org.florinmatei.homework.server.Session;
import org.florinmatei.homework.server.SessionFactory;

/**
 *
 * @author Florin Matei
 */
public class HttpSessionFactory implements SessionFactory {

    final HttpParser parser;
    final BasicServer server;

    public HttpSessionFactory(final BasicServer server) {
        this.server = server;
        this.parser = new HttpParser(server);
    }

    @Override
    public Session createSession(final Socket socket) {
//parser.execute(socket);
//        final Request request = new HttpRequest(metod, uri, query, headers, parameters, cookieHandler);
//        final Response response = new Response(Status.OK, MimeType.TEXT_HTML, "");
        return new HttpSession(socket, parser);//request, response);
    }
}
