/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package functional;

import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.florinmatei.homework.test.ServerBaseTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Florin Matei
 */
public class CookieTest extends ServerBaseTest {

    /**
     * Call a handler that will return no cookie
     *
     * @throws Exception
     */
    @Test
    public void testNoCookies() throws Exception {
        HttpGet httpget = new HttpGet("http://localhost:8080/noCookie");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        httpclient.execute(httpget, responseHandler);

        CookieStore cookies = httpclient.getCookieStore();
        assertEquals(0, cookies.getCookies().size());
    }

    /**
     * Now call one that should return a Cookie
     *
     * @throws Exception
     */
    @Test
    public void testReceiveCookie() throws Exception {
        HttpGet httpget = new HttpGet("http://localhost:8080/sendCookie");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        httpclient.execute(httpget, responseHandler);
        CookieStore cookies = httpclient.getCookieStore();
        assertEquals(1, cookies.getCookies().size());
        assertEquals("test", cookies.getCookies().get(0).getName());
        assertEquals("test", cookies.getCookies().get(0).getValue());
    }
}
