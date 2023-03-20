package org.itmo.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * Class store base info about file
 */
@Getter
public class FileInfo {
    /**
     * Name of file
     */
    private final String filename;
    
    /**
     * Size of the file
     */
    private final long fileSize;
    
    /**
     * Last position inside the file when it was opened
     */
    @Setter
    private long position;
    
    public FileInfo(String filename, long fileSize) {
        this.filename = filename;
        this.fileSize = fileSize;
        position = 0;
    }
}
