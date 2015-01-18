/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import org.florinmatei.homework.server.ServiceProvider;
import org.florinmatei.homework.server.Session;
import org.florinmatei.homework.server.exceptions.ResponseException;

/**
 * API setting the contract for a FileManager.
 *
 * @author Florin Matei
 */
public interface FileManager extends ServiceProvider {

    /**
     * Creates a Temporary File
     *
     * @return the newly created temporary file
     */
    public ServerFile createTemporaryFile();

    /**
     * Creates a long term file.
     *
     * @param session
     * @param fileName
     * @return
     */
    public ServerFile createFile(Session session, String fileName);

    public RandomAccessFile createTemporaryRandomAccessFile();

    /**
     * saves a temporary file
     *
     * @param b
     * @param offset
     * @param len
     * @return
     */
    public String saveTemporaryFile(final ByteBuffer b, final int offset, final int len);

    /**
     * Do security checks on the current session and make sure the session is
     * allowed to access the uri
     *
     * @param session the current session
     * @param uri the uri that needs to be accessed
     * @return true if the uri can be accessed by the current session
     */
    public boolean hasAccessToFile(final Session session, final String uri);

    /**
     * Returns the file found at the location given by uri. This method will
     * return null if the URI is not a child of the root folder. If access
     * privileges are not met a ResponseException will be thrown
     *
     * @param session
     * @param uri
     * @return
     * @throws java.io.IOException
     * @throws ResponseException
     */
    public ServerFile getFile(final Session session, final String uri) throws ResponseException, IOException;

    public boolean fileExists(final String uri);
}
