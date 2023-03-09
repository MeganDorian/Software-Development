package org.itmo.exceptions;

public class FlagNotFoundException extends Exception {
    
    /**
     * An exception occurs if an incorrect flag is entered for the command
     *
     * @param message -- error information
     */
    public FlagNotFoundException(String message) {
        super(message);
    }
}
