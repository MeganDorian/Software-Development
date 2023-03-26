package org.itmo.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtilsForTest {
    
    /**
     * Loads content from the file. Use this if you sure that file isn't large
     *
     * @param f file to read
     * @return file content
     */
    public static String loadFullContent(File f) {
        try (RandomAccessFile file = new RandomAccessFile(f, "r")) {
            long fileSize = file.length();
            byte[] c = new byte[(int) fileSize];
            file.readFully(c);
            return new String(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
