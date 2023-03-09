package org.itmo.commands;

import org.itmo.commands.cat.Cat;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileInfo;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
    
    @BeforeAll
    public static void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
        inputStream = System.in;
        outputStream = System.out;
    }
    
    private String loadResult() {
        return FileUtils.loadFullContent(CommandResultSaver.getResult().toFile())
                .replaceAll("\r", "").replaceAll("\n", "");
    }
    
    @Test
    public void shouldReadFromInputStream() {
        CommandInfo info = new CommandInfo("cat/cat1", Collections.emptyList(), Collections.emptyList());
        String expected = "test";
        InputStream forTests = new ByteArrayInputStream((expected + "\n").getBytes());
        System.setIn(forTests);
        Cat cat = new Cat(info);
        assertDoesNotThrow(cat::execute);
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldPrintHelp() {
        String expected = "Usage: cat [OPTION]... [FILE]..." +
                "Concatenate FILE(s) to standard output.With no FILE read standard input.    " +
                "-e              - display $ at end of each line    " +
                "-n              - number all output lines    " +
                "--h, --help     - display this help and exit";
        CommandInfo info = new CommandInfo("cat/cat1", List.of("--help"), Collections.emptyList());
        Cat cat = new Cat(info);
        assertDoesNotThrow(cat::execute);
        
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldGetFilesContent() throws URISyntaxException {
        File file = new File(Objects.requireNonNull(
                this.getClass().getClassLoader().getResource("cat/cat1")).toURI());
        String expected = FileUtils.loadFullContent(file)
                .replaceAll("\r", "").replaceAll("\n", "");
        File file2 = new File(Objects.requireNonNull(
                this.getClass().getClassLoader().getResource("cat/cat2")).toURI());
        expected += FileUtils.loadFullContent(file2)
                .replaceAll("\r", "").replaceAll("\n", "");
        
        CommandInfo info = new CommandInfo("cat", Collections.emptyList(),
                List.of(file.getAbsolutePath(), file2.getAbsolutePath()));
        Cat cat = new Cat(info);
        assertDoesNotThrow(cat::execute);
        
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldGetFileContentWithFlags() throws URISyntaxException {
        File file = new File(Objects.requireNonNull(
                this.getClass().getClassLoader().getResource("cat/cat3")).toURI());
        FileInfo fileInfo = FileUtils.getFileInfo("cat/cat3");
        
        StringBuilder content = new StringBuilder();
        int lineCount = 1;
        while (fileInfo.getPosition() < fileInfo.getFileSize()) {
            content.append("\t").append(lineCount).append("\t\t");
            FileUtils.loadLineFromFile(fileInfo).ifPresent(content::append);
            content.append("$");
            lineCount++;
        }
        String expected = content.toString().replaceAll("\r", "").replaceAll("\n", "");
        
        
        CommandInfo info = new CommandInfo("cat", List.of("-e", "-n"),
                List.of(file.getAbsolutePath()));
        Cat cat = new Cat(info);
        assertDoesNotThrow(cat::execute);
        
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldThrowFileNotFoundException() {
        CommandInfo info = new CommandInfo("cat/cat1", List.of("-e", "-n"), List.of("fdfsdfe"));
        Cat cat = new Cat(info);
        assertThrows(CatFileNotFoundException.class, cat::execute);
    }
    
    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.clearCommandResult();
        System.setIn(inputStream);
        System.setOut(outputStream);
    }
}
