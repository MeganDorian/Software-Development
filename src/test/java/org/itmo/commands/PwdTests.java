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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PwdTests {
    
    @BeforeAll
    public static void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
    }
    
    @Test
    public void shouldGetCurrentWorkingDirectory() {
        String expected = System.getProperty("user.dir");
        CommandInfo commandInfo = new CommandInfo("pwd", Collections.emptyList(), Collections.emptyList());
    
        Pwd pwd = new Pwd(commandInfo);
        pwd.execute();
        
        String actual = FileUtils.loadFullContent(CommandResultSaver.getResult().toFile());
        assertEquals(expected, actual);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.clearCommandResult();
    }
}
