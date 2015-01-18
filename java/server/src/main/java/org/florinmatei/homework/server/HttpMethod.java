/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server;

/**
 *
 * @author Florin Matei
 */
public enum HttpMethod {

    GET,
    PUT,
    POST,
    DELETE,
    HEAD,
    OPTIONS;

    static HttpMethod getHttpMethod(final String method) {
        HttpMethod returnValue = null;
        if (method != null) {

            for (HttpMethod m : HttpMethod.values()) {
                if (m.toString().equalsIgnoreCase(method)) {
                    returnValue = m;
                }
            }
        }

        return returnValue;
    }
}
