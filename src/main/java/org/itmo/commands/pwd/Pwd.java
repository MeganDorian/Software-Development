package org.itmo.commands.pwd;

import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.utils.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PWD command to print current directory
 */
public class Pwd implements Command {
    private List<PwdFlags> flags;
    
    public Pwd(CommandInfo commandInfo) {
        flags = new ArrayList<>();
        commandInfo.getFlags().forEach(flag -> flags.add(PwdFlags.valueOf(flag.replaceAll("^-{1,2}", "").toUpperCase())));
    }
    
    @Override
    public void execute() {
        if (!printHelp()) {
            String currentDirectory = System.getProperty("user.dir");
            CommandResultSaver.saveCommandResult(currentDirectory, false);
        }
    }
    
    @Override
    public void execute(InputStream stream) throws Exception {
    
    }
    
    @Override
    public boolean printHelp() {
        if (!flags.isEmpty() && flags.contains(PwdFlags.HELP)) {
            FileInfo helpInfo = FileUtils.getFileInfo(ResourcesLoader.getProperty(Commands.pwd + ".help"), true);
            while (helpInfo.getPosition() < helpInfo.getFileSize()) {
                Optional<String> line = FileUtils.loadLineFromFile(helpInfo);
                line.ifPresent(l -> CommandResultSaver.saveCommandResult(l, true));
            }
            return true;
        }
        return false;
    }
}
