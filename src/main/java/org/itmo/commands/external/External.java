package org.itmo.commands.external;

import org.itmo.commands.Command;
import org.itmo.exceptions.ExternalException;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    public void execute() throws ExternalException {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            StringBuilder paramWithFlags = new StringBuilder(String.join(" ", params));
            if (isWindows) {
                builder.command("cmd.exe", "/c", name + " " + paramWithFlags);
            }
            else {
                builder.command("sh", "-c", name + " " + paramWithFlags);
            }
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            if (process.waitFor() != 0) {
                StringBuilder error = new StringBuilder();
                while ((line = readerError.readLine()) != null) {
                    error.append(line + "\n");
                }
                throw new ExternalException(error.toString());
            }
            while ((line = reader.readLine()) != null) {
                CommandResultSaver.saveCommandResult(line, true);
            }
        } catch (Exception ex) {
            throw new ExternalException(ex.getMessage());
        }
    }
    
    @Override
    public boolean printHelp () {
        return false;
    }
    
}
