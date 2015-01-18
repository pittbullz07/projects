/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.florinmatei.homework.file;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.florinmatei.homework.utils.CloseableUtils;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Florin Matei
 */
public class DefaultFileManagerTest {

    private static final String FILE_GOOD = "testFile2.temp";
    private static final String FILE_BAD = "../testFile.temp";
    static DefaultFileManager fileManager;
    static File root;
    static File tempFile;
    static File tempFile2;

    public DefaultFileManagerTest() {
    }

    @BeforeClass
    public static void setUp() throws Exception {
        root = new File(".");
        tempFile = prepareFile(FILE_BAD);
        tempFile2 = prepareFile(FILE_GOOD);
        fileManager = new DefaultFileManager(new File("."));
    }

    @AfterClass
    public static void tearDown() {
        tempFile.delete();
        tempFile2.delete();
        fileManager.cleanTemporaryFiles();
    }

    /**
     * Test of createTemporaryFile method, of class DefaultFileManager.
     */
    @Test
    public void testCreateTemporaryFile() {
        System.out.println("createTemporaryFile");
        ServerFile result = fileManager.createTemporaryFile();
        assertNotNull(result);
    }

    /**
     * Test of createTemporaryRandomAccessFile method, of class
     * DefaultFileManager.
     */
    @Test
    public void testCreateTemporaryRandomAccessFile() {
        System.out.println("createTemporaryRandomAccessFile");
        RandomAccessFile result = fileManager.createTemporaryRandomAccessFile();
        CloseableUtils.safeClose(result);
        assertNotNull(result);
    }

    /**
     * Test of saveTemporaryFile method, of class DefaultFileManager.
     */
    @Test
    public void testSaveTemporaryFile() throws Exception {
        System.out.println("saveTemporaryFile");
        byte[] buffer = new byte[20];

        for (int i = 0; i < buffer.length; ++i) {
            buffer[i] = (byte) ('a' + i);
        }
        ByteBuffer b = ByteBuffer.wrap(buffer);

        int offset = 0;
        int len = b.capacity();
        String result = fileManager.saveTemporaryFile(b, offset, len);
        ServerFile file = fileManager.getFile(null, result);
        InputStream fs = file.read();
        byte[] actual = new byte[20];
        fs.read(actual);
        fs.close();
        assertTrue(Arrays.equals(b.array(), actual));
    }

    /**
     * Test of getFile method, of class DefaultFileManager.
     */
    @Test
    public void testGetFile() throws Exception {
        //no support yet for sessions so this test will fail when it wil lbe added
        try {
            fileManager.getFile(null, "../testFIle.temp");
            //an exception should have been thrown
            fail();
        }
        catch (Exception e) {
        }
    }

    /**
     * Test of fileExists method, of class DefaultFileManager.
     */
    @Test
    public void testFileExists() {
        System.out.println("fileExists");
        boolean result = fileManager.fileExists(FILE_GOOD);
        assertTrue(result);
        result = fileManager.fileExists(FILE_BAD);
        assertFalse(result);
    }

    private static File prepareFile(String fileName) throws Exception {
        File file = new File(root, fileName);
        file.createNewFile();
        file.deleteOnExit();
        return file;
    }
}
