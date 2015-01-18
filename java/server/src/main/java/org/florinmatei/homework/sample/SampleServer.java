/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.sample;

import java.util.Map;
import java.util.Properties;
import java.util.Random;
import org.florinmatei.homework.annotations.Context;
import org.florinmatei.homework.handler.Handler;
import org.florinmatei.homework.server.BasicServer;
import org.florinmatei.homework.server.HttpMethod;
import org.florinmatei.homework.server.Session;

/**
 *
 * @author Florin Matei
 */
public class SampleServer extends BasicServer {

    public SampleServer(Properties properties) {
        super(properties);
        super.registerHandler(new Hand(this));
    }

    public static class Hand extends Handler {

        public Hand(BasicServer server) {
            super(server);
        }

        @Context(method = HttpMethod.GET, url = "/hello_world")
        public void handle(Session session) {
            session.getResponse().setData("<html>Hello WWW!</html>");
        }

        @Context(method = HttpMethod.GET, url = "/game")
        public void getGamePage(Session session) {
            final String page = createGamePage(null);
            session.getResponse().setData(page);
        }

        @Context(method = HttpMethod.POST, url = "/game")
        public void postGamePage(Session session) {

            final Map<String, String> parameters = session.getRequest().getParameters();

            final String get = parameters.get("number");
            int userNumber = -1;
            try {
                userNumber = Integer.parseInt(get);
            }
            catch (NumberFormatException ex) {
            }

            String message;
            if (userNumber == -1) {
                message = " The number needs to be positive<br><br>";
            } else {
                Random r = new Random();
                int random = Math.max(0, r.nextInt());
                message = "My number was: " + random;
                message += " and your number was: " + userNumber;
                message += "<br>";
                message += random > userNumber ? (random + "&gt; " + userNumber + "! I win<br><br>") : (random + "&lt;" + userNumber + " You win! Lucky lucky.. Go again?<br><br>");
            }

            final String page = createGamePage(message);
            session.getResponse().setData(page);
        }

        private String createGamePage(String message) {
            return "<html><body><form name=\"input\" action=\"game\" method=\"post\">\n"
                    + (message == null ? "" : message)
                    + "Enter a number: <input type=\"number\" name=\"number\">\n"
                    + "<input type=\"submit\" value=\"Submit\">\n"
                    + "</form></body></html>";
        }
    }
}
