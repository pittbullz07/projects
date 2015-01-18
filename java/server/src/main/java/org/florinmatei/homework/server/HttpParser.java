/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.florinmatei.homework.file.FileManager;
import org.florinmatei.homework.server.exceptions.ResponseException;
import org.florinmatei.homework.server.http.HttpConstants;
import org.florinmatei.homework.server.http.HttpRequest;
import static org.florinmatei.homework.utils.CloseableUtils.safeClose;

/**
 *
 * @author Florin Matei
 */
public class HttpParser {

    public static final String HEADER_CONTENT_LENGTH = "content-length";
    private static final String KEY_URI = "uri";
    private static final String KEY_QUERY_STRING = "query";
    private static final String KEY_METHOD = "method";
    public static final int BUFSIZE = 8192;
    private final BasicServer server;
    private final FileManager fileManager;
    private final Logger LOG;

    public HttpParser(BasicServer server) {
        this.server = server;
        this.fileManager = (FileManager) server.lookupServiceProvider(FileManager.class);
        this.LOG = Logger.getLogger(server.getServerName());
    }

    public HttpRequest parse(Socket readSocket) throws IOException, ResponseException {
        int splitBytePosition;
        int totalCharsReadCount;
        final PushbackInputStream pushBackInputStream = new PushbackInputStream(readSocket.getInputStream(), BUFSIZE);
        final Map<String, String> parameters = new HashMap<String, String>();
        final Map<String, String> headers = new HashMap<String, String>();
        final Map<String, String> pre = new HashMap<String, String>();
        final HttpMethod method;
        final InetAddress inetAddress = readSocket.getInetAddress();
        final String remoteIp = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "127.0.0.1" : inetAddress.getHostAddress().toString();
        final CookieContainer cookies;
        headers.put("remote-addr", remoteIp);
        headers.put("http-client-ip", remoteIp);
        try {
            // Read the first 8192 bytes.
            // The full header should fit in here.
            // Apache's default header limit is 8KB.
            // Do NOT assume that a single read will get the entire header at once!
            byte[] buf = new byte[BUFSIZE];
            splitBytePosition = 0;
            totalCharsReadCount = 0;
            int read = -1;
            try {
                read = pushBackInputStream.read(buf, 0, BUFSIZE);
            }
            catch (Exception e) {
                safeClose(pushBackInputStream);
                throw new SocketException("NanoHttpd Shutdown");
            }
            if (read == -1) {
                // socket was closed
                safeClose(pushBackInputStream);
                throw new SocketException("NanoHttpd Shutdown");
            }
            while (read > 0) {
                totalCharsReadCount += read;
                splitBytePosition = findHeaderEnd(buf, totalCharsReadCount);
                if (splitBytePosition > 0) {
                    break;
                }
                read = pushBackInputStream.read(buf, totalCharsReadCount, BUFSIZE - totalCharsReadCount);
            }

            if (splitBytePosition < totalCharsReadCount) {
                pushBackInputStream.unread(buf, splitBytePosition, totalCharsReadCount - splitBytePosition);
            }

            // Create a BufferedReader for parsing the header.
            BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, totalCharsReadCount)));

            // Decode the header into parms and header java properties
            decodeHeader(hin, pre, parameters, headers);

            try {
                method = HttpMethod.valueOf(pre.get(KEY_METHOD));
            }
            catch (IllegalArgumentException iae) {
                throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Syntax error.");
            }

            long bodySize;
            if (headers.containsKey(HEADER_CONTENT_LENGTH)) {
                bodySize = Integer.parseInt(headers.get(HEADER_CONTENT_LENGTH));
            } else if (splitBytePosition < totalCharsReadCount) {
                bodySize = totalCharsReadCount - splitBytePosition;
            } else {
                bodySize = 0;
            }
            Map<String, String> files = new HashMap<>();
            parseBody(pushBackInputStream, method, files, parameters, headers, bodySize);

            cookies = new CookieContainer(headers);

        }
        catch (SocketException | SocketTimeoutException | ResponseException ex) {
            // throw it out to close socket object (finalAccept)
            throw ex;
        }
        catch (IOException ioe) {
            throw new ResponseException(Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        }

        return new HttpRequest(method, pre.get(KEY_URI), pre.get(KEY_QUERY_STRING), headers, parameters, cookies);
    }

    private void parseBody(
            final InputStream inputStream,
            final HttpMethod method,
            final Map<String, String> files,
            final Map<String, String> parameters,
            final Map<String, String> headers,
            final long bodySize) throws IOException, ResponseException {
        long size = bodySize;
        int rlen = 0;
        RandomAccessFile randomAccessFile = null;
        BufferedReader in = null;
        try {
            randomAccessFile = fileManager.createTemporaryRandomAccessFile();
            // Now read all the body and write it to f
            byte[] buf = new byte[512];
            while (rlen >= 0 && size > 0) {
                rlen = inputStream.read(buf, 0, (int) Math.min(size, 512));
                size -= rlen;
                if (rlen > 0) {
                    randomAccessFile.write(buf, 0, rlen);
                }
            }

            // Get the raw body as a byte []
            ByteBuffer fbuf = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
            randomAccessFile.seek(0);

            // Create a BufferedReader for easily reading it as string.
            InputStream bin = new FileInputStream(randomAccessFile.getFD());
            in = new BufferedReader(new InputStreamReader(bin));

            // If the method is POST, there may be parameters
            // in data section, too, read it:
            if (HttpMethod.POST.equals(method)) {
                String contentType = "";
                String contentTypeHeader = headers.get(HttpConstants.HeaderConstants.HEADER_CONTENT_TYPE);

                StringTokenizer st = null;
                if (contentTypeHeader != null) {
                    st = new StringTokenizer(contentTypeHeader, ",; ");
                    if (st.hasMoreTokens()) {
                        contentType = st.nextToken();
                    }
                }

                if (MimeType.MULTIPART_FORM.equalsIgnoreCase(contentType)) {
                    // Handle multipart/form-data
                    if (!st.hasMoreTokens()) {
                        throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
                    }

                    String boundaryStartString = "boundary=";
                    int boundaryContentStart = contentTypeHeader.indexOf(boundaryStartString) + boundaryStartString.length();
                    String boundary = contentTypeHeader.substring(boundaryContentStart, contentTypeHeader.length());
                    if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                        boundary = boundary.substring(1, boundary.length() - 1);
                    }

                    decodeMultipartData(boundary, fbuf, in, parameters, files);
                } else {
                    String postLine = "";
                    StringBuilder postLineBuffer = new StringBuilder();
                    char pbuf[] = new char[512];
                    int read = in.read(pbuf);
                    while (read >= 0 && !postLine.endsWith("\r\n")) {
                        postLine = String.valueOf(pbuf, 0, read);
                        postLineBuffer.append(postLine);
                        read = in.read(pbuf);
                    }
                    postLine = postLineBuffer.toString().trim();
                    // Handle application/x-www-form-urlencoded
                    if (MimeType.FORM_ENCODED.equalsIgnoreCase(contentType)) {
                        decodeParms(postLine, parameters);
                    } else if (postLine.length() != 0) {
                        // Special case for raw POST data => create a special files entry "postData" with raw content data
                        files.put("postData", postLine);
                    }
                }
            } else if (HttpMethod.PUT.equals(method)) {
                files.put("content", fileManager.saveTemporaryFile(fbuf, 0, fbuf.limit()));
            }
        }
        finally {
            safeClose(randomAccessFile);
            safeClose(in);
        }
    }

    /**
     * Decodes the sent headers and loads the data into Key/value pairs
     */
    private void decodeHeader(final BufferedReader in,
            final Map<String, String> pre,
            final Map<String, String> parameters,
            final Map<String, String> headers) throws ResponseException {
        try {
            // Read the request line
            String inLine = in.readLine();
            if (inLine == null) {
                return;
            }

            final StringTokenizer st = new StringTokenizer(inLine);
            if (!st.hasMoreTokens()) {
                throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
            }

            pre.put(KEY_METHOD, st.nextToken());

            if (!st.hasMoreTokens()) {
                throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
            }

            final String url = URLDecoder.decode(st.nextToken(), "UTF-8");
            final String uri;
            final String query;
            // Decode parameters from the URI
            final int qmi = url.indexOf('?');
            if (qmi >= 0) {
                query = url.substring(qmi + 1);
                decodeParms(query, parameters);
                uri = url.substring(0, qmi);
            } else {
                query = "";
                uri = url;
            }

            // If there's another token, it's protocol version,
            // followed by HTTP headers. Ignore version but parse headers.
            // NOTE: this now forces header names lowercase since they are
            // case insensitive and vary by client.
            if (st.hasMoreTokens()) {
                String line = in.readLine();
                while (line != null && line.trim().length() > 0) {
                    int p = line.indexOf(':');
                    if (p >= 0) {
                        headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
                    }
                    line = in.readLine();
                }
            }

            pre.put(KEY_URI, uri);
            pre.put(KEY_QUERY_STRING, query);
        }
        catch (IOException ioe) {
            throw new ResponseException(Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Decodes the Multipart Body data and put it into Key/Value pairs.
     */
    private void decodeMultipartData(String boundary, ByteBuffer fbuf, BufferedReader in, Map<String, String> parms,
            Map<String, String> files) throws ResponseException {
        try {
            int[] bpositions = getBoundaryPositions(fbuf, boundary.getBytes());
            int boundarycount = 1;
            String mpline = in.readLine();
            while (mpline != null) {
                if (!mpline.contains(boundary)) {
                    throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary. Usage: GET /example/file.html");
                }
                boundarycount++;
                Map<String, String> item = new HashMap<String, String>();
                mpline = in.readLine();
                while (mpline != null && mpline.trim().length() > 0) {
                    int p = mpline.indexOf(':');
                    if (p != -1) {
                        item.put(mpline.substring(0, p).trim().toLowerCase(Locale.US), mpline.substring(p + 1).trim());
                    }
                    mpline = in.readLine();
                }
                if (mpline != null) {
                    String contentDisposition = item.get("content-disposition");
                    if (contentDisposition == null) {
                        throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but no content-disposition info found. Usage: GET /example/file.html");
                    }
                    StringTokenizer st = new StringTokenizer(contentDisposition, ";");
                    Map<String, String> disposition = new HashMap<String, String>();
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken().trim();
                        int p = token.indexOf('=');
                        if (p != -1) {
                            disposition.put(token.substring(0, p).trim().toLowerCase(Locale.US), token.substring(p + 1).trim());
                        }
                    }
                    String pname = disposition.get("name");
                    pname = pname.substring(1, pname.length() - 1);

                    String value = "";
                    if (item.get(HttpConstants.HeaderConstants.HEADER_CONTENT_TYPE) == null) {
                        while (mpline != null && !mpline.contains(boundary)) {
                            mpline = in.readLine();
                            if (mpline != null) {
                                int d = mpline.indexOf(boundary);
                                if (d == -1) {
                                    value += mpline;
                                } else {
                                    value += mpline.substring(0, d - 2);
                                }
                            }
                        }
                    } else {
                        if (boundarycount > bpositions.length) {
                            throw new ResponseException(Status.INTERNAL_ERROR, "Error processing request");
                        }
                        int offset = stripMultipartHeaders(fbuf, bpositions[boundarycount - 2]);
                        String path = fileManager.saveTemporaryFile(fbuf, offset, bpositions[boundarycount - 1] - offset - 4);
                        files.put(pname, path);
                        value = disposition.get("filename");
                        value = value.substring(1, value.length() - 1);
                        do {
                            mpline = in.readLine();
                        } while (mpline != null && !mpline.contains(boundary));
                    }
                    parms.put(pname, value);
                }
            }
        }
        catch (IOException ioe) {
            throw new ResponseException(Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Find byte index separating header from body. It must be the last byte of
     * the first two sequential new lines.
     */
    private int findHeaderEnd(final byte[] buf, int rlen) {
        int splitbyte = 0;
        while (splitbyte + 3 < rlen) {
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n') {
                return splitbyte + 4;
            }
            splitbyte++;
        }
        return 0;
    }

    /**
     * Find the byte positions where multipart boundaries start.
     */
    private int[] getBoundaryPositions(ByteBuffer b, byte[] boundary) {
        int matchcount = 0;
        int matchbyte = -1;
        List<Integer> matchbytes = new ArrayList<Integer>();
        for (int i = 0; i < b.limit(); i++) {
            if (b.get(i) == boundary[matchcount]) {
                if (matchcount == 0) {
                    matchbyte = i;
                }
                matchcount++;
                if (matchcount == boundary.length) {
                    matchbytes.add(matchbyte);
                    matchcount = 0;
                    matchbyte = -1;
                }
            } else {
                i -= matchcount;
                matchcount = 0;
                matchbyte = -1;
            }
        }
        int[] ret = new int[matchbytes.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = matchbytes.get(i);
        }
        return ret;
    }

    /**
     * It returns the offset separating multipart file headers from the file's
     * data.
     */
    private int stripMultipartHeaders(ByteBuffer b, int offset) {
        int i;
        for (i = offset; i < b.limit(); i++) {
            if (b.get(i) == '\r' && b.get(++i) == '\n' && b.get(++i) == '\r' && b.get(++i) == '\n') {
                break;
            }
        }
        return i + 1;
    }

    /**
     * Decodes parameters in percent-encoded URI-format ( e.g.
     * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given Map.
     * NOTE: this doesn't support multiple identical keys due to the simplicity
     * of Map.
     */
    private void decodeParms(final String parameterString, final Map<String, String> parameterMap) {
        if (parameterString == null) {
            return;
        }

        final StringTokenizer st = new StringTokenizer(parameterString, "&");
        while (st.hasMoreTokens()) {
            String keyValuePair = st.nextToken();
            int separator = keyValuePair.indexOf('=');
            if (separator >= 0) {
                parameterMap.put(keyValuePair.substring(0, separator).trim(), keyValuePair.substring(separator + 1));
            } else {
                parameterMap.put(keyValuePair.trim(), "");
            }
        }
    }
}
