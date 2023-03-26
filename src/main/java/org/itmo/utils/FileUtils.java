package org.itmo.utils;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility class to work with files
 */
@UtilityClass
public class FileUtils {
    
    /**
     * Load file from resources
     *
     * @param fileName file name to load
     * @return stream with content of loaded file
     */
    public InputStream getFileFromResource(String fileName) {
        InputStream stream = FileUtils.class.getClassLoader().getResourceAsStream(fileName);
        if (stream == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        return stream;
    }
    
    /**
     * Get InputStream of the file
     *
     * @param path absolute path to the file
     * @return InputStream of the file
     */
    public InputStream getFileAsStream(String path) {
        InputStream stream;
        try {
            stream = Files.newInputStream(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("File not found: " + path);
        }
        return stream;
    }
    
}
