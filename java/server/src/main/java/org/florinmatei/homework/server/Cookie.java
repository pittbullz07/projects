/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Cookie {

    public static String getHttpTime(final int days) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return dateFormat.format(calendar.getTime());
    }

    private final String name, value, expiration;
    private String domain = "";
    private String path = "";

    public Cookie(final String name, final String value) {
        this(name, value, 30);
    }

    public Cookie(final String name, final String value, final String expires) {
        this.name = name;
        this.value = value;
        this.expiration = expires;
    }

    public Cookie(final String name, final String value, final int numDays) {
        this.name = name;
        this.value = value;
        this.expiration = getHttpTime(numDays);
    }

    public String getHttpHeader() {
        String fmt = "%s=%s; expires=%s";
        return String.format(fmt, name, value, expiration);
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }
}
