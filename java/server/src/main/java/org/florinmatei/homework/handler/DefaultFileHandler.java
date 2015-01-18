/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.handler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.florinmatei.homework.file.DefaultServerFile;
import org.florinmatei.homework.file.FileManager;
import org.florinmatei.homework.file.ServerFile;
import org.florinmatei.homework.server.BasicServer;
import org.florinmatei.homework.server.MimeType;
import org.florinmatei.homework.server.Response;
import org.florinmatei.homework.server.Session;
import org.florinmatei.homework.server.Status;
import org.florinmatei.homework.server.exceptions.ResponseException;

/**
 * Default implementation for the FileHandler. If no other implementation is
 * provided this will be used by default.
 *
 * @author Florin Matei
 */
public class DefaultFileHandler extends FileHandler {

    private static final String[] INDEX_FILE_NAMES = {"index.html", "index.htm"};
    private static final String ROOT = "/";

    public DefaultFileHandler(final BasicServer server) {
        super(server);
    }

    @Override
    public void handle(final Session session) throws Exception {
        FileManager fileManager = (FileManager) server.lookupServiceProvider(FileManager.class);
        String uri = session.getRequest().getURI();

        if (ROOT.equals(uri)) {
            for (String index : INDEX_FILE_NAMES) {
                String indexUri = ROOT + index;
                if (fileManager.fileExists(indexUri)) {
                    uri = indexUri;
                    break;
                }
            }
        }

        final boolean hasAccessToFile = fileManager.hasAccessToFile(session, uri);
        if (!hasAccessToFile) {
            throw new ResponseException(Status.FORBIDDEN, "Cannot Acces the file!");
        }

        ServerFile file = fileManager.getFile(session, uri);

        if (file == null) {
            throw new ResponseException(Status.NOT_FOUND, "No such file - " + uri);
        }
        final Response response = session.getResponse();
        response.setStatus(Status.OK);
        if (file.isDirectory()) {
            response.setData(listDirectory(uri, file));
            response.setMimeType(MimeType.TEXT_HTML);
        } else {
            response.setData(file.read());
            String mimeType = MimeType.getMimeTypeFromFileName(file.getName());
            mimeType = mimeType == null ? MimeType.TEXT_PLAIN : mimeType;
            response.setMimeType(mimeType);
        }
    }

    protected String listDirectory(String uri, ServerFile f) {
        String heading = "Directory " + uri;
        StringBuilder msg = new StringBuilder("<html><head><title>" + heading + "</title><style><!--\n"
                + "span.dirname { font-weight: bold; }\n"
                + "span.filesize { font-size: 75%; }\n"
                + "// -->\n"
                + "</style>"
                + "</head><body><h1>" + heading + "</h1>");

        String up = null;
        if (uri.length() > 1) {
            String u = uri.substring(0, uri.length() - 1);
            int slash = u.lastIndexOf('/');
            if (slash >= 0 && slash < u.length()) {
                up = uri.substring(0, slash + 1);
            }
        }
        //@ TODO HACKY AND NEEDS TO BE FIXED
        File realFile = DefaultServerFile.getFile(f);
        List<String> files = Arrays.asList(realFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        }));
        Collections.sort(files);
        List<String> directories = Arrays.asList(realFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        }));
        Collections.sort(directories);
        if (up != null || directories.size() + files.size() > 0) {
            msg.append("<ul>");
            if (up != null || directories.size() > 0) {
                msg.append("<section class=\"directories\">");
                if (up != null) {
                    msg.append("<li><a rel=\"directory\" href=\"").append(up).append("\"><span class=\"dirname\">..</span></a></b></li>");
                }
                for (String directory : directories) {
                    String dir = directory + "/";
                    msg.append("<li><a rel=\"directory\" href=\"").append(encodeUri(uri + dir)).append("\"><span class=\"dirname\">").append(dir).append("</span></a></b></li>");
                }
                msg.append("</section>");
            }
            if (files.size() > 0) {
                msg.append("<section class=\"files\">");
                for (String file : files) {
                    msg.append("<li><a href=\"").append(encodeUri(uri + "/" + file)).append("\"><span class=\"filename\">").append(file).append("</span></a>");
                    File curFile = new File(realFile, file);
                    long len = curFile.length();
                    msg.append("&nbsp;<span class=\"filesize\">(");
                    if (len < 1024) {
                        msg.append(len).append(" bytes");
                    } else if (len < 1024 * 1024) {
                        msg.append(len / 1024).append(".").append(len % 1024 / 10 % 100).append(" KB");
                    } else {
                        msg.append(len / (1024 * 1024)).append(".").append(len % (1024 * 1024) / 10 % 100).append(" MB");
                    }
                    msg.append(")</span></li>");
                }
                msg.append("</section>");
            }
            msg.append("</ul>");
        }
        msg.append("</body></html>");
        return msg.toString();
    }

    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/")) {
                newUri += "/";
            } else if (tok.equals(" ")) {
                newUri += "%20";
            } else {
                try {
                    newUri += URLEncoder.encode(tok, "UTF-8");
                }
                catch (UnsupportedEncodingException ignored) {
                }
            }
        }
        return newUri;
    }
}
