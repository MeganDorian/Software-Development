package org.itmo.commands;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.itmo.utils.FileUtils;
import org.itmo.utils.ResourcesLoader;
import org.itmo.utils.command.CommandResultSaver;

public interface Command {
    
    /**
     * Executes the command
     */
    void execute() throws Exception;
    
    boolean printHelp() throws IOException;
    
    default void print(Commands command) throws IOException {
        String helpFileName = ResourcesLoader.getProperty(command + ".help");
        try (BufferedReader reader = getReader(FileUtils.getFileFromResource(helpFileName))) {
            while (reader.ready()) {
                CommandResultSaver.writeToOutput(reader.readLine() + "\n", APPEND_TO_OUTPUT);
            }
        }
    }
    
    default BufferedReader getReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
}
