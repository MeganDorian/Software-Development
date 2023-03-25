package org.itmo.commands.pwd;

import static org.itmo.utils.command.CommandResultSaverFlags.NOT_APPEND_TO_OUTPUT;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.utils.command.CommandResultSaver;

/**
 * PWD command to print current directory
 */
@Parameters(commandDescription = "PWD command to print current directory")
@AllArgsConstructor
@NoArgsConstructor
public class Pwd implements Command {
    
    @Parameter(names = {"--help", "-h"}, description = "display this help and exit", help = true)
    private boolean help;
    
    @Getter
    private static String currentDirectory = System.getProperty("user.dir");
    
    @Override
    public void execute() throws IOException {
        if (printHelp()) {
            return;
        }
        CommandResultSaver.writeToOutput(currentDirectory, NOT_APPEND_TO_OUTPUT);
    }
    
    @Override
    public boolean printHelp() throws IOException {
        if (help) {
            print(Commands.pwd);
            return true;
        }
        return false;
    }
    
    @Override
    public Commands getCommandName() {
        return Commands.pwd;
    }
}
