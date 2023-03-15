package org.itmo.commands.echo;

import org.itmo.commands.Command;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;

import java.util.List;
import java.util.Objects;

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
    public void execute() {
        if (paramsToPrint.isEmpty()) {
            CommandResultSaver.savePipeCommandResult("\n");
        } else {
            paramsToPrint.forEach(s -> CommandResultSaver.savePipeCommandResult(
                    s + (!Objects.equals(s, paramsToPrint.get(paramsToPrint.size() - 1)) ? " " : "")
            ));
        }
    }
    
    @Override
    public boolean printHelp() {
        return false;
    }
}
