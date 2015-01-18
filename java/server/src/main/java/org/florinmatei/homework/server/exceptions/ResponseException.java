/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server.exceptions;

import org.florinmatei.homework.server.Status;

/**
 *
 * @author Florin Matei
 */
public class ResponseException extends Exception {

    private final Status status;

    public ResponseException(Status status, String message) {
        this(status, message, null);
    }

    public ResponseException(Status status, String message, Exception e) {
        super(message, e);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
