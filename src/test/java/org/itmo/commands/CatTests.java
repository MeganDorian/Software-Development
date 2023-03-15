package org.itmo.commands;

import org.itmo.commands.cat.Cat;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileInfo;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class CatTests {
    static InputStream inputStream;
    static PrintStream outputStream;
    
    @BeforeEach
    public void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
        CommandResultSaver.savePipeCommandResult("");
        inputStream = System.in;
        outputStream = System.out;
    }
    
    private String loadResult() {
        return FileUtils.loadFullContent(CommandResultSaver.getResult().toFile())
                .replaceAll("\r", "").replaceAll("\n", "");
    }
    
    private void checkResult(String expected, CommandInfo info) {
        Cat cat = new Cat(info);
        CommandResultSaver.saveCommandResult();
        assertDoesNotThrow(cat::execute);
        CommandResultSaver.saveCommandResult();
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldReadFromInputStream() {
        CommandInfo info = new CommandInfo("cat", Collections.emptyList(),
                List.of(CommandResultSaver.getResultPath()));
        String expected = "test";
        InputStream forTests = new ByteArrayInputStream((expected + "\n").getBytes());
        System.setIn(forTests);
        checkResult(expected, info);
    }
    
    @Test
    public void shouldPrintHelp() {
        String expected = "Usage: cat [OPTION]... [FILE]..." + "Concatenate FILE(s) to standard output.With no FILE read standard input.    " + "-e              - display $ at end of each line    " + "-n              - number all output lines    " + "--h, --help     - display this help and exit";
        CommandInfo info = new CommandInfo("cat", List.of("--help"), Collections.emptyList());
        checkResult(expected, info);
    }
    
    @Test
    public void shouldGetFilesContent() throws URISyntaxException {
        File file = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("cat/cat1")).toURI());
        File file2 = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("cat/cat2")).toURI());
        
        String expected = FileUtils.loadFullContent(file).replaceAll("\r", "").replaceAll("\n", "");
        expected += FileUtils.loadFullContent(file2).replaceAll("\r", "").replaceAll("\n", "");
        
        CommandInfo info = new CommandInfo("cat", Collections.emptyList(), List.of(file.getAbsolutePath(), file2.getAbsolutePath()));
        checkResult(expected, info);
    }
    
    @Test
    public void shouldGetFileContentWithFlags() throws URISyntaxException {
        File file = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("cat/cat3")).toURI());
        FileInfo fileInfo = FileUtils.getFileInfo("cat/cat3", true);
        
        int lineCount = 1;
        StringBuilder content = new StringBuilder(FileUtils.loadFullContent(file));
        content.insert(0, "\t" + lineCount + "\t\t");
        int nextPosToReplace = content.indexOf("\r\n");
        while (nextPosToReplace != -1) {
            lineCount++;
            content.replace(nextPosToReplace, nextPosToReplace + 2, "$\t" + lineCount + "\t\t");
            nextPosToReplace = content.indexOf("\r\n");
        }
        String expected = content.toString() + "$";
        
        
        CommandInfo info = new CommandInfo("cat", List.of("-e", "-n"), List.of(file.getAbsolutePath()));
        checkResult(expected, info);
    }
    
    @Test
    public void shouldThrowFileNotFoundException() {
        CommandInfo info = new CommandInfo("cat", List.of("-e", "-n"), List.of("hello there"));
        Cat cat = new Cat(info);
        assertThrows(CatFileNotFoundException.class, cat::execute);
    }
    
    @AfterEach
    public void cleanUp() {
        CommandResultSaver.deleteCommandResult();
        System.setIn(inputStream);
        System.setOut(outputStream);
    }
}
