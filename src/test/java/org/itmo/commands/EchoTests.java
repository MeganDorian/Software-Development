package org.itmo.commands;

import org.itmo.commands.echo.Echo;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtilsForTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.itmo.commands.Commands.echo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EchoTests {
    @BeforeEach
    public void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
        CommandResultSaver.savePipeCommandResult("");
    }
    
    private void checkResult(String expected, CommandInfo info) {
        Echo echo = new Echo(info);
        assertDoesNotThrow(echo::execute);
        CommandResultSaver.saveCommandResult();
        String actual = FileUtilsForTest.loadFullContent(CommandResultSaver.getResult().toFile())
                .replace("\r", "").replace("\n", "");
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldPrintValues() {
        CommandInfo info = new CommandInfo(echo, Collections.emptyList(), List.of("Obi", "Wan", "Kenobi"));
        checkResult("Obi Wan Kenobi", info);
    }
    
    @Test
    public void shouldPrintEmptyString() {
        CommandInfo info = new CommandInfo(echo, Collections.emptyList(), Collections.emptyList());
        checkResult("", info);
    }
    
    @AfterEach
    public void cleanUp() {
        CommandResultSaver.deleteCommandResult();
    }
}
