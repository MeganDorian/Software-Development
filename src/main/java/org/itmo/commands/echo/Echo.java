package org.itmo.commands.echo;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;
import static org.itmo.utils.command.CommandResultSaverFlags.NOT_APPEND_TO_OUTPUT;

import java.io.IOException;
import java.util.List;
import org.itmo.commands.Command;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.command.CommandResultSaver;

/**
 * ECHO command to print in the console
 */
public class Echo implements Command {
    /**
     * Content to print
     */
    private final List<String> paramsToPrint;
    
    public Echo(CommandInfo commandInfo) {
        paramsToPrint = commandInfo.getParams();
    }
    
    /**
     * Prints content. If no content was passed to the command, prints empty string
     */
    @Override
    public void execute() throws IOException {
        if (paramsToPrint.isEmpty()) {
            CommandResultSaver.writeToOutput("\n", NOT_APPEND_TO_OUTPUT);
        } else {
            CommandResultSaver.writeToOutput(String.join(" ", paramsToPrint), APPEND_TO_OUTPUT);
        }
    }
    
    @Override
    public boolean printHelp() {
        return false;
    }
}
