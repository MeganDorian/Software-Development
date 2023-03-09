package org.itmo.exceptions;

public class FlagNotFound extends Exception {
    
    /**
     * An exception occurs if an incorrect flag is entered for the command
     *
     * @param message -- error information
     */
    public FlagNotFound(String message) {
        super(message);
    }
}
