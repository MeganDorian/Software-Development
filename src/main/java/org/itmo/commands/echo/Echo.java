package org.itmo.commands.echo;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;
import static org.itmo.utils.command.CommandResultSaverFlags.NOT_APPEND_TO_OUTPUT;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.utils.command.CommandResultSaver;

/**
 * ECHO command to print in the console
 */
@Parameters(commandDescription = "ECHO command to print in the console")
@AllArgsConstructor
@NoArgsConstructor
public class Echo implements Command {
    /**
     * Content to print
     */
    @Getter
    @Parameter(description = "content to print")
    private List<String> paramsToPrint;
    
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
    
    @Override
    public Commands getCommandName() {
        return Commands.echo;
    }
}
