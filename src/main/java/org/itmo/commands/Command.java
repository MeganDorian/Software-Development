package org.itmo.commands;

public interface Command {
    
    /**
     * Executes command
     */
    void execute() throws Exception;
    
    boolean printHelp();
}
