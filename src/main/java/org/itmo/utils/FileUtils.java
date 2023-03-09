package org.itmo.utils;

import lombok.experimental.UtilityClass;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class to work with files
 */
@UtilityClass
public class FileUtils {
    /**
     * Finds file in the resources folder and collects information about it
     *
     * @param filename name of file
     * @return FileInfo with information about file
     */
    public FileInfo getFileInfo(String filename, boolean fromResources) {
        try {
            URI uri =  Objects.requireNonNull(FileUtils.class.getClassLoader().getResource(filename).toURI());
            File file = new File(fromResources ? uri.getPath() : filename);
            if (!file.exists() || !file.isFile()) {
                throw new FileNotFoundException("No file with name" + filename + " found");
            }
            return new FileInfo(file.getAbsolutePath(), file.length());
        } catch (FileNotFoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Load file from resources
     *
     * @param fileName file name to load
     * @return stream with content of loaded file
     */
    public InputStream getFileFromResource(String fileName) {
        InputStream stream = ResourcesLoader.class.getClassLoader().getResourceAsStream(fileName);
        if (stream == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        return stream;
    }
    
    /**
     * Reads one line from the file from the pos
     *
     * @param info FileInfo with information about file
     * @return one line from the file or null if end of file reached
     */
    public Optional<String> loadLineFromFile(FileInfo info) {
        try (RandomAccessFile file = new RandomAccessFile(new File(info.getFilename()), "r")) {
            if (info.getPosition() < file.length()) {
                file.seek(info.getPosition());
                String r = file.readLine();
                info.setPosition(file.getFilePointer());
                return Optional.of(r);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Loads content from the file. Use this if you sure that file isn't large
     *
     * @param f file to read
     * @return file content
     */
    public String loadFullContent(File f) {
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
