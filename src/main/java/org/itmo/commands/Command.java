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
    
    /**
     * If flag -h or --help passed to the command, prints help
     *
     * @return true if the help was printed
     *
     * @throws IOException if unable to write to the common output stream
     */
    boolean printHelp() throws IOException;
    
    /**
     * Opens file with help info about the command and writes it content to the common output
     * stream
     *
     * @param command command name
     *
     * @throws IOException if unable to write to the common output stream
     */
    default void print(Commands command) throws IOException {
        String helpFileName = ResourcesLoader.getProperty(command + ".help");
        try (BufferedReader reader = getReader(FileUtils.getFileFromResource(helpFileName))) {
            while (reader.ready()) {
                CommandResultSaver.writeToOutput(reader.readLine() + "\n", APPEND_TO_OUTPUT);
            }
        }
    }
    
    /**
     * Opens buffered reader from the passed input stream with the standard encoding UTF-8
     *
     * @param inputStream stream to read
     *
     * @return opened buffered reader
     */
    default BufferedReader getReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
    
    /**
     * @return name of the command
     */
    Commands getCommandName();
}
