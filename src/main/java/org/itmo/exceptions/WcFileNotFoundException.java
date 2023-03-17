package org.itmo.exceptions;

public class WcFileNotFoundException extends Exception {
    /**
     * Exception arises if wc command not found the file
     *
     * @param message message with additional info about exception
     */
    public WcFileNotFoundException(String message) {
        super(message);
    }
}
