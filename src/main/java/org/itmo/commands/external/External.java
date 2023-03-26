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

/**
 * EXTERNAL command
 */
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
    
    /**
     * Retrieves output content from the process, print it to the system out and to the common
     * output stream
     *
     * @param process started process which executes external command
     * @param reader  connected to the output stream of the process reader
     *
     * @throws InterruptedException if process interrupted while waiting
     * @throws IOException          if unable to write to the common output stream
     */
    private void retrieveContentFromProcess(Process process, BufferedReader reader)
        throws InterruptedException, IOException {
        String line;
        do {
            process.waitFor(10, TimeUnit.NANOSECONDS);
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                CommandResultSaver.writeToOutput(line, APPEND_TO_OUTPUT);
            }
        } while (process.isAlive());
    }
    
    /**
     * Handles if during the process execution any error was written to the error stream
     *
     * @param process     with started external command
     * @param readerError reader connected to the error stream of the process
     *
     * @throws ExternalException if during the process any exception was thrown
     * @throws IOException       If an I/O error occurs
     */
    private void handleError(Process process, BufferedReader readerError)
        throws ExternalException, IOException {
        String line;
        if (process.exitValue() != 0) {
            StringBuilder error = new StringBuilder();
            while ((line = readerError.readLine()) != null) {
                error.append(line).append("\n");
            }
            throw new ExternalException(error.toString());
        }
    }
    
    /**
     * Handles the process work
     *
     * @param process with started external command
     *
     * @throws IOException       if unable to write to the common output stream
     * @throws ExternalException if during the process any exception was thrown
     */
    private void executeProcess(Process process) throws IOException, ExternalException {
        try (BufferedReader readerError = getReader(process.getErrorStream());
             BufferedReader reader = getReader(FileUtils.getFileAsStream(
                 CommandResultSaver.getExternalResult().toAbsolutePath().toString()))) {
            retrieveContentFromProcess(process, reader);
            handleError(process, readerError);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Runs users command. Creates temporary file to write the result of the command. Deletes it
     * after the execution
     *
     * @throws ExternalException if during the command execution thread threw IOException
     * @throws IOException       if during the creation of temporary file threw IOException
     */
    @Override
    public void execute() throws ExternalException, IOException {
        ProcessBuilder builder = new ProcessBuilder();
        CommandResultSaver.createTemporaryFileForExternal();
        builder.command(isWindows ? "cmd.exe" : "sh", isWindows ? "/c" : "-c",
                        String.join(" ", params)).redirectInput(ProcessBuilder.Redirect.INHERIT)
               .redirectOutput(CommandResultSaver.getExternalResult().toFile());
        try {
            Process process = builder.start();
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
