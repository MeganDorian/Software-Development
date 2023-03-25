package org.itmo.commands.external;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.ExternalException;
import org.itmo.utils.FileUtils;
import org.itmo.utils.command.CommandResultSaver;


@Parameters(commandDescription = "any external command")
@AllArgsConstructor
public class External implements Command {
    
    @Getter
    @Parameter(description = "full external command with flags")
    private List<String> params;
    
    private final boolean isWindows;
    
    public External() {
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
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
        ProcessBuilder builder = new ProcessBuilder();
        CommandResultSaver.createTemporaryFileForExternal();
        builder.command(isWindows ? "cmd.exe" : "sh", isWindows ? "/c" : "-c",
                        String.join(" ", params)).redirectInput(ProcessBuilder.Redirect.INHERIT)
               .redirectOutput(CommandResultSaver.getExternalResult().toFile());
        Process process = null;
        try {
            process = builder.start();
            executeProcess(process);
        } catch (IOException e) {
            throw new ExternalException(params.get(0) + " command not found");
        }
        CommandResultSaver.deleteTemporaryFileForExternal();
    }
    
    @Override
    public boolean printHelp() {
        return false;
    }
    
    @Override
    public Commands getCommandName() {
        return Commands.external;
    }
    
}
