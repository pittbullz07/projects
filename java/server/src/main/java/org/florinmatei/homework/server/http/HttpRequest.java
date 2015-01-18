/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server.http;

import java.util.Map;
import org.florinmatei.homework.server.CookieContainer;
import org.florinmatei.homework.server.HttpMethod;
import org.florinmatei.homework.server.Request;

/**
 *
 * @author Florin Matei
 */
public class HttpRequest implements Request {

    final HttpMethod method;
    final String uri;
    final Map<String, String> headers;
    final Map<String, String> parameters;
    final String queryString;
    final CookieContainer cookies;

    public HttpRequest(HttpMethod method, String uri, String queryString, Map<String, String> headers, Map<String, String> parameters, CookieContainer cookies) {
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.parameters = parameters;
        this.queryString = queryString;
        this.cookies = cookies;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String getURI() {
        return uri;
    }

    @Override
    public String getQueryParameterString() {
        return queryString;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public CookieContainer getCookies() {
        return cookies;
    }

    @Override
    public void destroy() {

    }

    @Override
    public String toString() {
        return method.toString() + " " + uri + " " + queryString;
    }

}
