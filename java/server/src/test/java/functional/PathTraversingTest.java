/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package functional;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.florinmatei.homework.test.ServerBaseTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Florin Matei
 */
public class PathTraversingTest extends ServerBaseTest {

    @Test
    public void testTraversalDenied() throws Exception {
        HttpGet httpget = new HttpGet("http://localhost:8080/../../");

        CloseableHttpResponse execute = httpclient.execute(httpget);
        execute.close();
        assertEquals(403, execute.getStatusLine().getStatusCode());
    }

    @Test
    public void testNoTraversal() throws Exception {
        HttpGet httpget = new HttpGet("http://localhost:8080/");

        CloseableHttpResponse execute = httpclient.execute(httpget);
        execute.close();
        assertEquals(200, execute.getStatusLine().getStatusCode());
    }
}
