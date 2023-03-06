package org.itmo.utils;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Saves result of the command to the temporary file
 */
@UtilityClass
public class CommandResultSaver {
    private static final String commandResult = ResourcesLoader.getProperty("commandResult");
    
    /**
     * Saves result of command execution to temporary file
     *
     * @param content content to save
     */
    public void saveCommandResult(String content) {
        URL resource = CommandResultSaver.class.getClassLoader().getResource(commandResult);
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(Objects.requireNonNull(resource).toURI()), true)) {
            fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Deletes all content from file with results of command execution
     *
     * @throws IOException if can't get access to file
     */
    public void clearCommandResult() throws IOException {
        new FileOutputStream(ResourcesLoader.getProperty(commandResult)).close();
    }
    
    /**
     * Deletes temporary file
     *
     * @return true if temporary file successfully deleted <br>
     * false - otherwise
     */
    public boolean deleteCommandResult() {
        return new File(ResourcesLoader.getProperty(commandResult)).delete();
    }
}
