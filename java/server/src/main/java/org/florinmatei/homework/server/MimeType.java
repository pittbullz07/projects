/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florin Matei
 */
public final class MimeType {

    public static final String FORM_ENCODED = "application/x-www-form-urlencoded",
            MESSAGE_HTTP = "message/http",
            MULTIPART_BYTERANGES = "multipart/byteranges",
            MULTIPART_FORM = "multipart/form",
            TEXT_HTML = "text/html",
            TEXT_PLAIN = "text/plain",
            TEXT_XML = "text/xml",
            TEXT_JSON = "text/json",
            TEXT_HTML_8859_1 = "text/html;charset=ISO-8859-1",
            TEXT_PLAIN_8859_1 = "text/plain;charset=ISO-8859-1",
            TEXT_XML_8859_1 = "text/xml;charset=ISO-8859-1",
            TEXT_HTML_UTF_8 = "text/html;charset=UTF-8",
            TEXT_PLAIN_UTF_8 = "text/plain;charset=UTF-8",
            TEXT_XML_UTF_8 = "text/xml;charset=UTF-8",
            TEXT_JSON_UTF_8 = "text/json;charset=UTF-8";

    private static final String TEXT_HTML__8859_1 = "text/html; charset=ISO-8859-1",
            TEXT_PLAIN__8859_1 = "text/plain; charset=ISO-8859-1",
            TEXT_XML__8859_1 = "text/xml; charset=ISO-8859-1",
            TEXT_HTML__UTF_8 = "text/html; charset=UTF-8",
            TEXT_PLAIN__UTF_8 = "text/plain; charset=UTF-8",
            TEXT_XML__UTF_8 = "text/xml; charset=UTF-8",
            TEXT_JSON__UTF_8 = "text/json; charset=UTF-8";

    private static final Map<String, String> MIME_MAP = new HashMap();

    private final static Logger LOG = Logger.getLogger(BasicServer.class.getName());

    static {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("org/florinmatei/homework/resources/mimeTypes");
            Enumeration<String> mimes = bundle.getKeys();
            while (mimes.hasMoreElements()) {
                String extension = (String) mimes.nextElement();
                String mime = bundle.getString(extension);
                MIME_MAP.put(extension, mime);
            }
        }
        catch (MissingResourceException mre) {
            LOG.log(Level.WARNING, "Could not load mime resource bundle", mre);
        }
    }

    public static String getMimeTypeByExtension(final String fileName) {
        String returnValue = null;
        MIME_MAP.get(fileName);
        if (fileName != null && !fileName.isEmpty()) {
            final int lastIndexOf = fileName.indexOf('.');
            String extension = fileName.substring(lastIndexOf, fileName.length());

            if (extension.isEmpty()) {
                returnValue = MIME_MAP.get(extension);
            }
        }
        return returnValue;
    }

    public static String getMimeTypeFromFileName(final String fileName) {
        String returnValue = null;
        if (fileName != null && !fileName.isEmpty()) {
            final int lastIndexOf = fileName.lastIndexOf('.');
            if (lastIndexOf > 0) {
                String extension = fileName.substring(lastIndexOf + 1, fileName.length());
                if (!extension.isEmpty()) {
                    returnValue = MIME_MAP.get(extension);
                }
            }
        }

        return returnValue;
    }

    private MimeType() {
    }
}
