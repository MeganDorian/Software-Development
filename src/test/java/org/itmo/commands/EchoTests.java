package org.itmo.commands;

import static org.itmo.commands.Commands.echo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.itmo.commands.echo.Echo;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.command.CommandResultSaver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EchoTests {
    @BeforeEach
    public void setUp() {
        CommandResultSaver.initStreams();
    }
    
    @Test
    public void shouldPrintValues() {
        CommandInfo info =
            new CommandInfo(echo, Collections.emptyList(), List.of("Obi", "Wan", "Kenobi"));
        checkResult("Obi Wan Kenobi", info);
    }
    
    @Test
    public void shouldPrintEmptyString() {
        CommandInfo info = new CommandInfo(echo, Collections.emptyList(), Collections.emptyList());
        checkResult("", info);
    }
    
    private void checkResult(String expected, CommandInfo info) {
        Echo echo = new Echo(info);
        assertDoesNotThrow(echo::execute);
        String actual =
            new String(CommandResultSaver.getOutputStream().toByteArray()).replaceAll("\r", "")
                                                                          .replaceAll("\n", "");
        assertEquals(expected, actual);
    }
    
    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.closeStreams();
    }
}
