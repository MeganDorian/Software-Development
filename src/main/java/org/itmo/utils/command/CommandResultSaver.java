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
 * Saves result of the command to streams
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
    
    /**
     * Creates common empty input stream and common empty output stream
     */
    public void initStreams() {
        inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        outputStream = new ByteArrayOutputStream();
    }
    
    /**
     * Writes string to the common output stream
     *
     * @param content string to write
     * @param flag    APPEND_TO_OUTPUT if need to append to the already written to the output stream
     *                content.
     *                <p>
     *                NOT_APPEND_TO_OUTPUT if need to overwrite content in the common output stream
     *
     * @throws IOException if unable to write to the common output stream
     */
    public void writeToOutput(String content, CommandResultSaverFlags flag) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            if (flag == APPEND_TO_OUTPUT) {
                writer.append(content);
            } else {
                writer.write(content);
            }
        }
    }
    
    
    /**
     * Writes all content from common output stream to the input stream
     *
     * @throws IOException if unable to reopen input stream
     */
    public void writeToInput() throws IOException {
        inputStream.close();
        inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    }
    
    /**
     * Drops content written to the output stream
     */
    public void clearOutput() {
        outputStream.reset();
    }
    
    /**
     * Closes common streams
     *
     * @throws IOException if unable to close streams
     */
    public void closeStreams() throws IOException {
        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            throw new IOException("Can't close streams properly");
        }
    }
    
    /**
     * Creates temporary file for the external command in which output stream will be redirected
     *
     * @throws IOException if unable to create temporary file
     */
    public void createTemporaryFileForExternal() throws IOException {
        externalResult = Files.createTempFile(externalResultName, ".cli");
    }
    
    /**
     * Deletes temporary file created for the external command
     */
    public void deleteTemporaryFileForExternal() {
        if (externalResult != null) {
            externalResult.toFile().delete();
        }
    }
    
}
