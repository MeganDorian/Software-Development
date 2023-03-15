package org.itmo.commands;

import org.itmo.commands.pwd.Pwd;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PwdTests {
    
    @BeforeEach
    public void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
    }
    
    private void checkResult(String expected, CommandInfo info) {
        Pwd pwd = new Pwd(info);
        assertDoesNotThrow(pwd::execute);
        CommandResultSaver.saveCommandResult();
        String actual = FileUtils.loadFullContent(CommandResultSaver.getResult().toFile())
                .replaceAll("\r", "").replaceAll("\n", "");
        assertEquals(expected, actual);
    }
    
    
    @Test
    public void shouldGetCurrentWorkingDirectory() {
        CommandInfo commandInfo = new CommandInfo("pwd", Collections.emptyList(), Collections.emptyList());
        checkResult(System.getProperty("user.dir"), commandInfo);
    }
    
    @Test
    public void shouldGetHelp() {
        String expected = "pwd:" +
                "    Print the name of the current working directory." +
                "    Options:" +
                "      --help    - display this help and exit";
        CommandInfo commandInfo = new CommandInfo("pwd", List.of("--help"), Collections.emptyList());
        checkResult(expected, commandInfo);
    }
    
    @AfterEach
    public void cleanUp() {
        CommandResultSaver.deleteCommandResult();
    }
}
