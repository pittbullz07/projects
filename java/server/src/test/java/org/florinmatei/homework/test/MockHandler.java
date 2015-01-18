/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.test;

import java.io.IOException;
import java.io.PipedInputStream;
import org.florinmatei.homework.annotations.Context;
import org.florinmatei.homework.handler.Handler;
import org.florinmatei.homework.server.BasicServer;
import org.florinmatei.homework.server.Cookie;
import org.florinmatei.homework.server.CookieContainer;
import org.florinmatei.homework.server.HttpMethod;
import org.florinmatei.homework.server.Session;

/**
 *
 * @author Florin Matei
 */
public class MockHandler extends Handler {

    public MockHandler(BasicServer server) {
        super(server);
    }

    @Context(method = HttpMethod.GET, url = "/sendCookie")
    public void handleSendCookie(Session session) {
        session.getResponse().setData("<html></html>");
        CookieContainer cookies = session.getRequest().getCookies();
        Cookie cookie = new Cookie("test", "test");
        cookie.setPath("/sendCookie");
        cookie.setDomain("localhost");
        cookies.set(cookie);
    }

    @Context(method = HttpMethod.GET, url = "/noCookie")
    public void handleReceveCookie(Session session) {
        session.getResponse().setData("<html></html>");
    }

    @Context(method = HttpMethod.GET, url = "/chunkedResponse")
    public void responseChunked(Session session) {
        session.getResponse().setData(new ChunkedInputStream(new String[]{"This is a ", "test", "and another one"}));
        session.getResponse().setChunkedTransfer(true);
    }

    private static class ChunkedInputStream extends PipedInputStream {

        int chunk = 0;
        String[] chunks;

        private ChunkedInputStream(String[] chunks) {
            this.chunks = chunks;
        }

        @Override
        public synchronized int read(byte[] buffer) throws IOException {
// Too implementation-linked, but...
            int returnValue = 0;
            if (chunk < chunks.length) {
                for (int i = 0; i < chunks[chunk].length(); ++i) {
                    if (i < buffer.length) {
                        buffer[i] = (byte) chunks[chunk].charAt(i);
                        returnValue++;
                    } else {
                        returnValue = buffer.length;
                        break;
                    }
                }
                chunk++;
            }
            return returnValue;
        }
    }
}
