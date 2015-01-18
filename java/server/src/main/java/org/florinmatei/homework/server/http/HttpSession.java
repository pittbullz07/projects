/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.florinmatei.homework.server.HttpMethod;
import org.florinmatei.homework.server.HttpParser;
import org.florinmatei.homework.server.MimeType;
import org.florinmatei.homework.server.Request;
import org.florinmatei.homework.server.Response;
import org.florinmatei.homework.server.Session;
import org.florinmatei.homework.server.Status;
import org.florinmatei.homework.server.exceptions.ResponseException;
import org.florinmatei.homework.utils.CloseableUtils;

/**
 *
 * @author Florin Matei
 */
public class HttpSession implements Session {

    private Request request;
    private Response response;
    private final Socket socket;
    private final HttpParser parser;

    public HttpSession(final Socket socket, final HttpParser parser) {
        this.socket = socket;
        this.parser = parser;
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public Response getResponse() {
        return response;
    }

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    public void next() throws IOException, ResponseException {
        this.request = parser.parse(socket);
        this.response = new Response(Status.OK, MimeType.TEXT_HTML, "");
    }

    @Override
    public void sendResponse() throws Exception {
        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        final InputStream data = response.getData();
        final OutputStream outputStream = socket.getOutputStream();
        try {
            request.getCookies().unloadQueue(response);
            PrintWriter pw = new PrintWriter(outputStream);
            pw.print("HTTP/1.1 " + response.getStatus().getDescription() + " \r\n");

            pw.print("Content-Type: " + response.getMimeType() + "\r\n");

            final Map<String, String> headers = response.getHeader();
            if (request.getHeaders().get("Date") == null) {
                pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
            }

            for (String key : headers.keySet()) {
                String value = headers.get(key);
                pw.print(key + ": " + value + "\r\n");
            }

            sendConnectionHeaderIfNotAlreadyPresent(pw, headers);
            if (request.getMethod() != HttpMethod.HEAD && response.getChunckedTransfer()) {
                sendAsChunked(response.getData(), outputStream, pw);
            } else {
                int pending = data != null ? data.available() : 0;
                sendContentLengthHeaderIfNotAlreadyPresent(pw, headers, pending);
                pw.print("\r\n");
                pw.flush();
                sendAsFixedLength(data, outputStream, pending);
            }
            outputStream.flush();
            CloseableUtils.safeClose(data);
        }
        catch (IOException ioe) {
            // Couldn't write? No can do.
        }
    }

    protected void sendContentLengthHeaderIfNotAlreadyPresent(final PrintWriter pw, final Map<String, String> header, final int size) {
        if (!headerAlreadySent(header, "content-length")) {
            pw.print("Content-Length: " + size + "\r\n");
        }
    }

    protected void sendConnectionHeaderIfNotAlreadyPresent(final PrintWriter pw, final Map<String, String> header) {
        if (!headerAlreadySent(header, "connection")) {
            pw.print("Connection: keep-alive\r\n");
        }
    }

    private boolean headerAlreadySent(final Map<String, String> header, final String name) {
        boolean alreadySent = false;
        for (String headerName : header.keySet()) {
            alreadySent |= headerName.equalsIgnoreCase(name);
        }
        return alreadySent;
    }

    private void sendAsFixedLength(final InputStream is, final OutputStream outputStream, final int pending) throws IOException {
        if (request.getMethod() != HttpMethod.HEAD && is != null) {
            int BUFFER_SIZE = 16 * 1024;
            byte[] buff = new byte[BUFFER_SIZE];
            int toRead = pending;
            while (pending > 0) {
                int read = is.read(buff, 0, ((toRead > BUFFER_SIZE) ? BUFFER_SIZE : toRead));
                if (read <= 0) {
                    break;
                }
                outputStream.write(buff, 0, read);
                toRead -= read;
            }
        }
    }

    private void sendAsChunked(final InputStream is, final OutputStream outputStream, final PrintWriter pw) throws IOException {
        pw.print("Transfer-Encoding: chunked\r\n");
        pw.print("\r\n");
        pw.flush();
        int BUFFER_SIZE = 16 * 1024;
        byte[] CRLF = "\r\n".getBytes();
        byte[] buff = new byte[BUFFER_SIZE];
        int read;
        while ((read = is.read(buff)) > 0) {
            outputStream.write(String.format("%x\r\n", read).getBytes());
            outputStream.write(buff, 0, read);
            outputStream.write(CRLF);
        }
        outputStream.write(String.format("0\r\n\r\n").getBytes());
    }
}
