package org.itmo.commands.external;

import org.itmo.commands.Command;
import org.itmo.exceptions.ExternalException;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

public class External implements Command {
    
    private String name;
    
    private final List<String> params;
    
    boolean isWindows;
    
    public External(CommandInfo commandInfo) {
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        name = commandInfo.getCommandName();
        params = commandInfo.getParams();
    }
    
    /**
     *
     * Run users command
     */
    @Override
    public void execute() throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", name + params.toString());
        } else {
            builder.command("sh", "-c", name + params.toString());
        }
        builder.directory(new File(System.getProperty("user.home")));
//        builder.redirectInput()   //?
        Process process = builder.start();
        process.waitFor();
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                CommandResultSaver.saveCommandResult(line, true);
            }
        } catch (Exception ex) {
            throw new ExternalException(ex);
        }
    }
    
    @Override
    public boolean printHelp ()
    {
        return false;
    }
    
}
