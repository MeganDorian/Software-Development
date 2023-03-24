package org.itmo.commands.external;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.itmo.commands.Command;
import org.itmo.exceptions.ExternalException;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.FileUtils;
import org.itmo.utils.command.CommandResultSaver;

public class External implements Command {
    
    private final String name;
    
    private final List<String> params;
    
    private final boolean isWindows;
    
    public External(CommandInfo commandInfo) {
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        name = commandInfo.getFlags().get(0);
        params = commandInfo.getParams();
    }
    
    
    private void executeProcess(Process process) throws IOException, ExternalException {
        String line;
        try (BufferedReader readerError = getReader(process.getErrorStream());
             BufferedReader readerToPrintImmediately = getReader(FileUtils.getFileAsStream(
                 CommandResultSaver.getExternalResult().toAbsolutePath().toString()))) {
            
            do {
                process.waitFor(10, TimeUnit.NANOSECONDS);
                while ((line = readerToPrintImmediately.readLine()) != null) {
                    System.out.println(line);
                    CommandResultSaver.writeToOutput(line, APPEND_TO_OUTPUT);
                }
            } while (process.isAlive());
            
            if (process.exitValue() != 0) {
                StringBuilder error = new StringBuilder();
                while ((line = readerError.readLine()) != null) {
                    error.append(line).append("\n");
                }
                throw new ExternalException(error.toString());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Run users command
     */
    @Override
    public void execute() throws ExternalException, IOException {
        CommandResultSaver.createTemporaryFileForExternal();
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(isWindows ? "cmd.exe" : "sh", isWindows ? "/c" : "-c",
                        name + " " + String.join(" ", params))
               .redirectInput(ProcessBuilder.Redirect.INHERIT)
               .redirectOutput(CommandResultSaver.getExternalResult().toFile());
        Process process = builder.start();
        executeProcess(process);
        CommandResultSaver.deleteTemporaryFileForExternal();
    }
    
    @Override
    public boolean printHelp() {
        return false;
    }
    
}
