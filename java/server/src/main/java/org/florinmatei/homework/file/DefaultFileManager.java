/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.florinmatei.homework.server.BasicServer;
import org.florinmatei.homework.server.Session;
import org.florinmatei.homework.server.Status;
import org.florinmatei.homework.server.exceptions.ResponseException;
import org.florinmatei.homework.utils.CloseableUtils;

/**
 * Default implementation for the FileManager interface. This implementation
 * respects the contract but does not provide any kind of Security Access
 * protection although the proper hooks are set in place.
 *
 * @author Florin Matei
 */
public class DefaultFileManager implements FileManager {

    private static final String TEMP_FOLDER_NAME = "temp";
    private final Logger LOG = Logger.getLogger(BasicServer.class.getName());
    private final File rootDir;
    private final File tempDir;

    public DefaultFileManager(File rootDir) {
        this.rootDir = rootDir;
        this.tempDir = new File(rootDir, TEMP_FOLDER_NAME);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
    }

    @Override
    public ServerFile createTemporaryFile() {
        ServerFile returnValue = null;
        try {
            returnValue = new DefaultServerFile(tempDir, true, null);
        }
        catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Could not create temporary file", ioe);
        }
        return returnValue;
    }

    @Override
    public RandomAccessFile createTemporaryRandomAccessFile() {
        try {
            final ServerFile tempFile = createTemporaryFile();
            return new RandomAccessFile(TEMP_FOLDER_NAME + File.separator + tempFile.getName(), "rw");
        }
        catch (Exception e) {
            throw new Error(e); // we won't recover, so throw an error
        }
    }

    @Override
    public String saveTemporaryFile(final ByteBuffer b, final int offset, final int len) {
        String path = null;
        if (len > 0) {
            FileOutputStream fileOutputStream = null;
            FileChannel dest = null;
            try {
                final ServerFile tempFile = createTemporaryFile();
                ByteBuffer src = b.duplicate();
                fileOutputStream = new FileOutputStream(TEMP_FOLDER_NAME + File.separator + tempFile.getName());
                dest = fileOutputStream.getChannel();
                src.position(offset).limit(offset + len);
                dest.write(src.slice());
                path = TEMP_FOLDER_NAME + File.separator + tempFile.getName();
            }
            catch (Exception e) { // Catch exception if any
                throw new Error(e); // we won't recover, so throw an error
            }
            finally {
                CloseableUtils.safeClose(dest);
                CloseableUtils.safeClose(fileOutputStream);
            }
        }
        return path;
    }

    @Override
    public boolean hasAccessToFile(final Session session, final String uri) {
        return true; //no security concers for this Manager
    }

    @Override
    public ServerFile getFile(final Session session, final String uri) throws ResponseException, IOException {
        File file = new File(rootDir, uri);
        if (file.exists()) {
            //if the new file path is not a child of the root dir then it's a no go
            if (!isChildFile(file)) {
                throw new ResponseException(Status.FORBIDDEN, "Cannot acces " + uri);
            }

            if (!hasAccessToFile(session, uri)) {
                throw new ResponseException(Status.FORBIDDEN, "Cannot access the file " + uri);
            }
        } else {
            file = null;
        }
        return file == null ? null : new DefaultServerFile(file, false);
    }

    @Override
    public ServerFile createFile(Session session, String fileName) {
        ServerFile returnValue = null;
        try {
            returnValue = getFile(session, fileName);
            if (returnValue == null) {
                returnValue = new DefaultServerFile(rootDir, false, fileName);
            }
        }
        catch (Exception e) {
        }
        return returnValue;
    }

    @Override
    public boolean fileExists(final String uri) {

        boolean returnValue = false;
        final File file = new File(rootDir, uri);
        if (file.exists()) {
            try {
                returnValue = isChildFile(file);
            }
            catch (IOException ioe) {
            }
        }
        return returnValue;
    }

    private boolean isChildFile(File file) throws IOException {
        String rootCanonicalPath = rootDir.getCanonicalPath();
        String fileCanonicalPath = file.getCanonicalPath();
        //if the new file path is not a child of the root dir then it's a no go
        return fileCanonicalPath.startsWith(rootCanonicalPath);
    }

    public void cleanTemporaryFiles() {
        File[] list = tempDir.listFiles();
        for (File s : list) {
            s.delete();
            System.out.println("Deleting " + s.getName());
        }
        tempDir.delete();
    }
}
