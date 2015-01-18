/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.florinmatei.homework.utils.CloseableUtils;

/**
 * Default strategy for creating and cleaning up temporary files.
 * <p/>
 * <p>
 * </p></[>By default, files are created by <code>File.createTempFile()</code>
 * in the directory specified.</p>
 *
 * @author NanoHTTPD
 */
public class DefaultServerFile implements ServerFile {

    private final File file;
    private OutputStream writeStream;
    private InputStream readStream;
    private final boolean isTemporary;

    public DefaultServerFile(final File rootDir, final boolean temporary, final String fileName) throws IOException {
        if (temporary) {
            isTemporary = true;
            file = File.createTempFile("server-", "", rootDir);
            file.deleteOnExit();
        } else {
            isTemporary = false;
            file = new File(rootDir, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        }
    }

    /**
     * Creates a wrapper around an existing file
     *
     * @param file
     * @param temporary
     * @throws IOException
     */
    public DefaultServerFile(final File file, final boolean temporary) throws IOException {
        if (temporary) {
            isTemporary = true;
        } else {
            isTemporary = false;
        }
        this.file = file;
    }

    @Override
    public void delete() throws SecurityException {
        CloseableUtils.safeClose(writeStream);
        file.delete();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public OutputStream write() throws IOException {
        if (writeStream == null) {
            writeStream = new FileOutputStream(file);
        }
        return writeStream;
    }

    public InputStream read() throws Exception {
        if (readStream == null) {
            readStream = new FileInputStream(file);
        }
        return readStream;
    }

    @Override
    public void close() throws IOException {
        CloseableUtils.safeClose(writeStream);
    }

    @Override
    public boolean isTemporary() {
        return isTemporary;
    }

    public static File getFile(ServerFile file) {
        return ((DefaultServerFile) file).file;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }
}
