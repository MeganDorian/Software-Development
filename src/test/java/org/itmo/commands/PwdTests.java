package org.itmo.commands;

import org.itmo.commands.pwd.Pwd;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PwdTests {
    
    @BeforeAll
    public static void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
    }
    
    private String loadResult() {
        return FileUtils.loadFullContent(CommandResultSaver.getResult().toFile())
                .replaceAll("\r", "").replaceAll("\n", "");
    }
    
    @Test
    public void shouldGetCurrentWorkingDirectory() {
        String expected = System.getProperty("user.dir");
        CommandInfo commandInfo = new CommandInfo("pwd", Collections.emptyList(), Collections.emptyList());
    
        Pwd pwd = new Pwd(commandInfo);
        pwd.execute();
        
        String actual = loadResult();
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldGetHelp() {
        String expected = "pwd:" +
                "    Print the name of the current working directory." +
                "    Options:" +
                "      --help    - display this help and exit";
        CommandInfo commandInfo = new CommandInfo("pwd", List.of("--help"), Collections.emptyList());
    
        Pwd pwd = new Pwd(commandInfo);
        pwd.execute();
        
        String actual = loadResult();
        assertEquals(expected, actual);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.clearCommandResult();
    }
}
