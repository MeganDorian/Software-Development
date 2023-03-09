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
    private final List<String> output;
    
    public Echo(CommandInfo commandInfo) {
        output = commandInfo.getParams();
    }
    
    /**
     * Prints content. If no content was passed to the command, prints empty string
     */
    @Override
    public void execute() {
        if (output.isEmpty()) {
            CommandResultSaver.saveCommandResult("", false);
        } else {
            output.forEach(s -> CommandResultSaver.saveCommandResult(
                    s + (!Objects.equals(s, output.get(output.size() - 1)) ? " " : ""),
                    false));
        }
    }
    
    @Override
    public boolean printHelp() {
        return false;
    }
}
