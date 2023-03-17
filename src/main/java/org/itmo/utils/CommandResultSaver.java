package org.itmo.utils;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Saves result of the command to the temporary file
 */
@UtilityClass
public class CommandResultSaver {
    
    private final String commandResult = ResourcesLoader.getProperty("commandResult");
    private final String pipeCommandResult = ResourcesLoader.getProperty("pipeResult");
    
    @Getter
    private static Path result;
    @Getter
    private static Path pipeResult;
    
    @Getter
    private static String resultPath;
    @Getter
    private static String pipeResultPath;
    
    public void createCommandResultFile() throws IOException {
        result = Files.createTempFile(commandResult, ".cli");
        pipeResult = Files.createTempFile(pipeCommandResult, ".cli");
        resultPath = result.toAbsolutePath().toString();
        pipeResultPath = pipeResult.toAbsolutePath().toString();
    }
    
    /**
     * Saves result of command execution to temporary piped file
     *
     * @param content content to save
     */
    public void savePipeCommandResult(String content) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(pipeResult.toFile(), true)) {
            fileOutputStream.write((content).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void saveFullPipeCommandResult(String fileName) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(FileUtils.getFileFromResource(fileName), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                CommandResultSaver.savePipeCommandResult(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Saves information from temporary pipe file to the command result file
     */
    public void saveCommandResult() {
        try (InputStream fileInputStream = FileUtils.getFileAsStream(pipeResultPath);
             FileOutputStream fileOutputStream = new FileOutputStream(result.toFile())) {
            int byteRead = fileInputStream.read();
            while (byteRead != -1) {
                fileOutputStream.write(byteRead);
                byteRead = fileInputStream.read();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Deletes all content from file with results of command execution
     *
     * @throws IOException if process can't get access to file
     */
    public void clearCommandResult() throws IOException {
        if (result != null) {
            try (PrintWriter print = new PrintWriter(result.toFile())) {
                print.print("");
            }
        }
    }
    
    public void clearPipeCommandResult() throws IOException {
        if (pipeResult != null) {
            try (PrintWriter print = new PrintWriter(pipeResult.toFile())) {
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
        return result != null && result.toFile().delete()
                && pipeResult != null && pipeResult.toFile().delete();
    }
}
