package org.itmo.utils;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Saves result of the command to the temporary file
 */
@UtilityClass
public class CommandResultSaver {
    
    private final String commandResult = ResourcesLoader.getProperty("commandResult");
    
    @Getter
    private Path result;
    
    /**
     * Saves result of command execution to temporary file
     *
     * @param content content to save
     */
    public void saveCommandResult(String content, boolean appendEndOfLine) {
        try {
            result = result == null ? Files.createTempFile(commandResult, ".cli") : result;
            try (FileOutputStream fileOutputStream = new FileOutputStream(result.toFile(), true)) {
                fileOutputStream.write((content + (appendEndOfLine ? "\n" : "")).getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Deletes all content from file with results of command execution
     *
     * @throws IOException if can't get access to file
     */
    public void clearCommandResult() throws IOException {
        if (result != null) {
            try (PrintWriter print = new PrintWriter(result.toFile())) {
                print.print("");
            }
        }
    }
    
    /**
     * Deletes temporary file
     *
     * @return true if temporary file successfully deleted <br>
     * false - otherwise
     */
    public boolean deleteCommandResult() {
        return result != null && result.toFile().delete();
    }
}
