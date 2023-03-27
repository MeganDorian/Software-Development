package org.itmo.exceptions;

public class GrepException extends Exception {
    /**
     * Exception arises if grep command not found the file
     *
     * @param message message with additional info about exception
     */
    public GrepException(String message) {
        super(message);
    }
}
