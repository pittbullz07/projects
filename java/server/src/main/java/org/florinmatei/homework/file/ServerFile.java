/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A temp file.
 * <p/>
 * <p>
 * Temp files are responsible for managing the actual temporary storage and
 * cleaning themselves up when no longer needed.</p>
 *
 * @author NanoHTTPD
 */
public interface ServerFile {

    public void delete() throws Exception;

    public String getName();

    public OutputStream write() throws Exception;

    public InputStream read() throws Exception;

    public void close() throws IOException;

    public boolean isTemporary();

    public boolean isDirectory();
}
