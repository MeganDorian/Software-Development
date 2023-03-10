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
    
    /**
     *
     * @param exception information on the exemption that has occurred
     */
    public FlagNotFoundException(Exception exception) {
        super(exception);
    }
}
