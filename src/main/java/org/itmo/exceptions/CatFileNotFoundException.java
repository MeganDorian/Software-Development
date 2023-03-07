package org.itmo.exceptions;

public class CatFileNotFoundException extends Exception {
    /**
     * Exception arises if cat command not found the file
     *
     * @param message message with additional info about exception
     */
    public CatFileNotFoundException(String message) {
        super(message);
    }
}
