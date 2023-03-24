package org.itmo.commands.pwd;

import static org.itmo.utils.command.CommandResultSaverFlags.NOT_APPEND_TO_OUTPUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.command.CommandResultSaver;

/**
 * PWD command to print current directory
 */
public class Pwd implements Command {
    private final List<PwdFlags> flags;
    
    @Getter
    private static String currentDirectory = System.getProperty("user.dir");
    
    public Pwd(CommandInfo commandInfo) {
        flags = new ArrayList<>();
        commandInfo.getFlags().forEach(
            flag -> flags.add(PwdFlags.valueOf(flag.replaceAll("^-{1,2}", "").toUpperCase())));
    }
    
    @Override
    public void execute() throws IOException {
        if (printHelp()) {
            return;
        }
        CommandResultSaver.writeToOutput(currentDirectory, NOT_APPEND_TO_OUTPUT);
    }
    
    @Override
    public boolean printHelp() throws IOException {
        if (!flags.isEmpty() && flags.contains(PwdFlags.HELP)) {
            print(Commands.pwd);
            return true;
        }
        return false;
    }
}
