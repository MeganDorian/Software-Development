package org.itmo.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.itmo.commands.pwd.Pwd;
import org.itmo.utils.command.CommandResultSaver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PwdTests {
    
    @BeforeEach
    public void setUp() {
        CommandResultSaver.initStreams();
    }
    
    @Test
    public void shouldGetCurrentWorkingDirectory() {
        Pwd pwd = new Pwd(false);
        checkResult(System.getProperty("user.dir"), pwd);
    }
    
    private void checkResult(String expected, Pwd pwd) {
        assertDoesNotThrow(pwd::execute);
        String actual =
            new String(CommandResultSaver.getOutputStream().toByteArray()).replaceAll("\r", "")
                                                                          .replaceAll("\n", "");
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldGetHelp() {
        Pwd pwd = new Pwd(true);
        String expected =
            "pwd:" + "    Print the name of the current working directory." + "    Options:" +
            "      --help    - display this help and exit";
        checkResult(expected, pwd);
    }
    
    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.closeStreams();
    }
}
