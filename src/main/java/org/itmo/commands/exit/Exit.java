package org.itmo.commands.exit;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.util.List;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.utils.command.CommandResultSaver;

/**
 * EXIT command to exit from the cli
 */
@Parameters(commandDescription = "EXIT command to exit from the cli")
public class Exit implements Command {
    
    @Parameter(description = "empty parameters")
    private List<String> params;
    
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
    
    @Override
    public Commands getCommandName() {
        return Commands.exit;
    }
}
