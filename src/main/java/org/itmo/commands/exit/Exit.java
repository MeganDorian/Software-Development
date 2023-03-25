package org.itmo.commands.exit;

import java.io.IOException;
import org.itmo.commands.Command;
import org.itmo.utils.command.CommandResultSaver;

/**
 * EXIT command to exit from the cli
 */
public class Exit implements Command {
    
    /**
     * Closes connected input and output streams
     *
     * @throws IOException if file deletion not successful
     */
    @Override
    public void execute() throws IOException {
        CommandResultSaver.closeStreams();
    }
    
    @Override
    public boolean printHelp() {
        return false;
    }
}
