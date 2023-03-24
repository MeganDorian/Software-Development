package org.itmo.utils.command;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.itmo.utils.ResourcesLoader;

/**
 * Saves result of the command to the temporary file
 */
@UtilityClass
public class CommandResultSaver {
    
    @Getter
    private ByteArrayInputStream inputStream;
    
    @Getter
    private ByteArrayOutputStream outputStream;
    
    private final String externalResultName = ResourcesLoader.getProperty("commandResult");
    
    @Getter
    private static Path externalResult;
    
    public void initStreams() {
        inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        outputStream = new ByteArrayOutputStream();
    }
    
    public void writeToOutput(String content, CommandResultSaverFlags flag) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            if (flag == APPEND_TO_OUTPUT) {
                writer.append(content);
            } else {
                writer.write(content);
            }
        }
    }
    
    public void writeToInput() throws IOException {
        inputStream.close();
        inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    }
    
    public void clearOutput() {
        outputStream.reset();
    }
    
    public void closeStreams() throws IOException {
        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            throw new IOException("Can't close streams properly");
        }
    }
    
    public void createTemporaryFileForExternal() throws IOException {
        externalResult = Files.createTempFile(externalResultName, ".cli");
    }
    
    public void deleteTemporaryFileForExternal() {
        if (externalResult != null) {
            externalResult.toFile().delete();
        }
    }
    
}
