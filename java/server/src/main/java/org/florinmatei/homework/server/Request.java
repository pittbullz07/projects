/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server;

import java.util.Map;

/**
 *
 * @author Florin Matei
 */
public interface Request {

    public Map<String, String> getHeaders();

    public Map<String, String> getParameters();

    public String getURI();

    public String getQueryParameterString();

    public HttpMethod getMethod();

    public CookieContainer getCookies();

    public void destroy();
}
