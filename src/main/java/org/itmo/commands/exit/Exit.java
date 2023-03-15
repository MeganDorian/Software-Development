package org.itmo.commands.exit;

import org.itmo.commands.Command;
import org.itmo.utils.CommandResultSaver;

import java.io.IOException;

/**
 * EXIT command to exit from the cli
 */
public class Exit implements Command {
    
    /**
     * Deletes temporary file for the command results and closes application
     *
     * @throws IOException if file deletion not successful
     */
    @Override
    public void execute() throws IOException {
        if (!CommandResultSaver.deleteCommandResult()) {
            throw new IOException("Can't delete temporary file");
        }
    }
    
    @Override
    public boolean printHelp() {
        return false;
    }
}
