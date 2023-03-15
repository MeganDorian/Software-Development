package org.itmo.commands;

import org.itmo.commands.wc.Wc;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WcTests {
    @BeforeAll
    public static void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
    }
    
    private String loadResult() {
        return FileUtils.loadFullContent(CommandResultSaver.getResult().toFile())
                .replaceAll("\r", "").replaceAll("\n", "");
    }
    
    private String getFilePath(String fileName) throws URISyntaxException {
        return new File(Objects.requireNonNull(
                this.getClass().getClassLoader().getResource(fileName)).toURI()).getAbsolutePath();
    }
    
    @Test
    public void shouldGetHelp() {
        String expected = "Usage: wc [OPTION]... [FILE]..." +
                "Print newline, word, and byte counts for each FILE, and a total line if" +
                "more than one FILE is specified.  A word is a non-zero-length sequence of" +
                "characters delimited by white space." +
                "With no FILE read standard input." +
                "The options below may be used to select which counts are printed, always in" +
                "the following order: newline, word, character, byte." +
                "  -c,               - print the byte counts" +
                "  -l                - print the newline counts" +
                "  -w                - print the word counts" +
                "  --h, --help       - display this help and exit";
        CommandInfo commandInfo = new CommandInfo("wc", List.of("-h"), Collections.emptyList());
        
        Wc wc = new Wc(commandInfo);
        assertDoesNotThrow(() -> wc.execute());
        
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    
    @Test
    public void shouldCountFromInputStream() {
        CommandInfo info = new CommandInfo("wc", Collections.emptyList(), Collections.emptyList());
        String test = "test df 1 g; asdsf ddd ";
        String expected = "\t1\t\t6\t\t22";
        InputStream forTests = new ByteArrayInputStream((test + "\n").getBytes());
        System.setIn(forTests);
        Wc wc = new Wc(info);
        assertDoesNotThrow(() -> wc.execute());
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldCountWithoutFlags() throws URISyntaxException {
        String file1 = getFilePath("wc/wc1");
        String file2 = getFilePath("wc/wc2");
        CommandInfo info = new CommandInfo("wc", Collections.emptyList(), List.of(file1, file2));
        String expected = "\t4\t\t16\t\t84\t\t" + file1
                + "\t4\t\t17\t\t105\t\t" + file2
                + "\t8\t\t33\t\t189\t\ttotal";
        
        Wc wc = new Wc(info);
        assertDoesNotThrow(() -> wc.execute());
        
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldCountWithOneFlag() throws URISyntaxException {
        String file1 = getFilePath("wc/wc1");
        CommandInfo info = new CommandInfo("wc", List.of("-c"), List.of(file1));
        String expected = "\t84\t\t" + file1;
        
        Wc wc = new Wc(info);
        assertDoesNotThrow(() -> wc.execute());
        
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldCountWithTwoFlag() throws URISyntaxException {
        String file1 = getFilePath("wc/wc1");
        CommandInfo info = new CommandInfo("wc", List.of("-c", "-l"), List.of(file1));
        String expected = "\t4\t\t84\t\t" + file1;
        
        Wc wc = new Wc(info);
        assertDoesNotThrow(() -> wc.execute());
        
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.clearCommandResult();
    }
}
