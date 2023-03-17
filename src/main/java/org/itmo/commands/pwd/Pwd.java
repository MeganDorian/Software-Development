package org.itmo.commands.pwd;

import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.ResourcesLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * PWD command to print current directory
 */
public class Pwd implements Command {
    private final List<PwdFlags> flags;
    
    public Pwd(CommandInfo commandInfo) {
        flags = new ArrayList<>();
        commandInfo.getFlags().forEach(flag -> flags.add(PwdFlags.valueOf(flag.replaceAll("^-{1,2}", "").toUpperCase())));
    }
    
    @Override
    public void execute() {
        if (!printHelp()) {
            CommandResultSaver.savePipeCommandResult(System.getProperty("user.dir"));
        }
    }
    
    @Override
    public boolean printHelp() {
        if (!flags.isEmpty() && flags.contains(PwdFlags.HELP)) {
            String helpFileName = ResourcesLoader.getProperty(Commands.pwd + ".help");
            CommandResultSaver.saveFullPipeCommandResult(helpFileName);
            return true;
        }
        return false;
    }
}
