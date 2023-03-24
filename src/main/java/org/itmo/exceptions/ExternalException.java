package org.itmo.exceptions;

public class ExternalException extends Exception {
    
    /**
     * Exceptions from external commands
     *
     * @param message cause of exception
     */
    public ExternalException(String message) {
        super(message);
    }
}
