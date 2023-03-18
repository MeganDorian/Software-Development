package org.itmo.commands.external;

import org.itmo.commands.Command;
import org.itmo.exceptions.ExternalException;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class External implements Command {
    
    private final String name;
    
    private final List<String> params;
    
    private final boolean isWindows;
    
    public External(CommandInfo commandInfo) {
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        name = commandInfo.getFlags().get(0);
        params = commandInfo.getParams();
    }
    
    /**
     * Run users command
     */
    @Override
    public void execute() throws ExternalException {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            StringBuilder paramWithFlags = new StringBuilder(String.join(" ", params));
            if (isWindows) {
                builder.command("cmd.exe", "/c", name + " " + paramWithFlags);
            } else {
                builder.command("sh", "-c", name + " " + paramWithFlags);
            }
            builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//          for cd - setting the start-up directory of the process
//            builder.directory(new File("D:\\LINK"));
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));
            BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream(),
                    StandardCharsets.UTF_8));
            String line;
            process.waitFor();
            while ((line = reader.readLine()) != null) {
                CommandResultSaver.savePipeCommandResult(line);
            }
            if (process.exitValue() != 0) {
                StringBuilder error = new StringBuilder();
                while ((line = readerError.readLine()) != null) {
                    error.append(line).append("\n");
                }
                throw new ExternalException(error.toString());
            }
        } catch (Exception ex) {
            throw new ExternalException(ex.getMessage());
        }
    }
    
    @Override
    public boolean printHelp() {
        return false;
    }
    
}
