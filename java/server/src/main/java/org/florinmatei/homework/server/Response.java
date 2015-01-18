/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florin Matei
 */
public class Response {

    /**
     * HTTP status code after processing, e.g. "200 OK", HTTP_OK
     */
    private Status status;
    /**
     * MIME type of content, e.g. "text/html"
     */
    private String mimeType;
    /**
     * Data of the response, may be null.
     */
    private InputStream data;
    /**
     * Headers for the HTTP response. Use addHeader() to add lines.
     */
    private Map<String, String> header = new HashMap<String, String>();
    /**
     * Use chunkedTransfer
     */
    private boolean chunkedTransfer;

    private final Logger LOG = Logger.getLogger(BasicServer.class.getName());

    /**
     * Default constructor: response = HTTP_OK, mime = MIME_HTML and your
     * supplied message
     */
    public Response(final String msg) {
        this(Status.OK, MimeType.TEXT_HTML, msg);
    }

    /**
     * Basic constructor.
     */
    public Response(final Status status, final String mimeType, final InputStream data) {
        this.status = status;
        this.mimeType = mimeType;
        this.data = data;
    }

    /**
     * Convenience method that makes an InputStream out of given text.
     */
    public Response(final Status status, final String mimeType, final String txt) {
        this.status = status;
        this.mimeType = mimeType;
        try {
            this.data = txt != null ? new ByteArrayInputStream(txt.getBytes("UTF-8")) : null;
        }
        catch (java.io.UnsupportedEncodingException uee) {
            LOG.log(Level.SEVERE, "", uee);
        }
    }

    /**
     * Adds given line to the header.
     */
    public void addHeader(final String name, final String value) {
        header.put(name, value);
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public InputStream getData() {
        return data;
    }

    public void setData(final String data) {
        try {
            this.data = new ByteArrayInputStream(data.getBytes("UTF-8"));
        }
        catch (java.io.UnsupportedEncodingException uee) {
            LOG.log(Level.SEVERE, "", uee);
        }
    }

    public void setData(final InputStream data) {
        this.data = data;
    }

    public void setChunkedTransfer(final boolean chunkedTransfer) {
        this.chunkedTransfer = chunkedTransfer;
    }

    public boolean getChunckedTransfer() {
        return chunkedTransfer;
    }

    @Override
    public String toString() {
        return status.toString();
    }
}
