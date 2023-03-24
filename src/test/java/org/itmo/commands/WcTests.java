package org.itmo.commands;

import static org.itmo.commands.Commands.wc;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.itmo.commands.wc.Wc;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WcTests {
    @BeforeEach
    public void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
        CommandResultSaver.savePipeCommandResult("");
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
        CommandInfo commandInfo = new CommandInfo(wc, List.of("-h"), Collections.emptyList());
        
        checkResult(expected, commandInfo);
    }
    
    private void checkResult(String expected, CommandInfo info) {
        Wc wc = new Wc(info);
        assertDoesNotThrow(wc::execute);
        CommandResultSaver.saveCommandResult();
        String actual =
            FileUtils.loadFullContent(CommandResultSaver.getResult().toFile()).replace("\r", "")
                .replace("\n", "");
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldCountFromInputStream() {
        CommandInfo info = new CommandInfo(wc, Collections.emptyList(),
            List.of(CommandResultSaver.getResultPath()));
        String test = "the force awakens ! \\";
        String expected = "\t1\t\t5\t\t21";
        InputStream forTests = new ByteArrayInputStream((test + "\n").getBytes());
        System.setIn(forTests);
        checkResult(expected, info);
    }
    
    @Test
    public void shouldCountWithoutFlags() throws URISyntaxException {
        String file1 = getFilePath("wc/wc1");
        String file2 = getFilePath("wc/wc2");
        CommandInfo info = new CommandInfo(wc, Collections.emptyList(), List.of(file1, file2));
        String expected = "\t5\t\t6\t\t31\t\t" + file1 + "\t4\t\t9\t\t50\t\t" + file2 +
                          "\t9\t\t15\t\t81\t\ttotal";
        
        checkResult(expected, info);
    }
    
    private String getFilePath(String fileName) throws URISyntaxException {
        return new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileName))
                .toURI()).getAbsolutePath();
    }
    
    @Test
    public void shouldCountWithOneFlag() throws URISyntaxException {
        String file1 = getFilePath("wc/wc1");
        CommandInfo info = new CommandInfo(wc, List.of("-c"), List.of(file1));
        String expected = "\t31\t\t" + file1;
        
        checkResult(expected, info);
    }
    
    @Test
    public void shouldCountWithTwoFlag() throws URISyntaxException {
        String file1 = getFilePath("wc/wc1");
        CommandInfo info = new CommandInfo(wc, List.of("-c", "-l"), List.of(file1));
        String expected = "\t5\t\t31\t\t" + file1;
        
        checkResult(expected, info);
    }
    
    @AfterEach
    public void cleanUp() {
        CommandResultSaver.deleteCommandResult();
    }
}
