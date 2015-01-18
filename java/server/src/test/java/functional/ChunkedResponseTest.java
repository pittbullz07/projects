/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package functional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import org.florinmatei.homework.test.ServerBaseTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Florin Matei
 */
public class ChunkedResponseTest extends ServerBaseTest {

    @Test
    public void testChunkedResponse() throws IOException {
        String expected = "HTTP/1.1 200 OK \r\n"
                + "Content-Type: text/html\r\n"
                + "Connection: keep-alive\r\n"
                + "Transfer-Encoding: chunked\r\n"
                + "\r\n"
                + "a\r\n"
                + "This is a \r\n"
                + "4\r\n"
                + "test\r\n"
                + "f\r\n"
                + "and another one\r\n"
                + "0\r\n\r\n";

        String response = getResponse("localhost", 8080, "/chunkedResponse");
        Assert.assertTrue(expected.length() == response.length());
        Assert.assertNotNull(response);
        Assert.assertEquals(expected, response);
    }

    private String getResponse(final String host, final int port, String url) {

        String returnValue = null;
        try (
                Socket socket = new Socket(host, port);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            writer.println("GET " + url + " HTTP/1.1");
            writer.println("Host: " + host);
            writer.println("Accept: */*");
            writer.println("User-Agent: Java");
            writer.println("");
            writer.flush();

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("Date: ")) {
                    builder.append(line).append("\r\n");
                }
            }
            returnValue = builder.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }
}
