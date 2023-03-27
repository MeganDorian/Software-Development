package org.itmo.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.itmo.commands.echo.Echo;
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
        Echo echo = new Echo(List.of("Obi", "Wan", "Kenobi"));
        checkResult("Obi Wan Kenobi", echo);
    }
    
    @Test
    public void shouldPrintEmptyString() {
        Echo echo = new Echo(Collections.emptyList());
        checkResult("", echo);
    }
    
    private void checkResult(String expected, Command echo) {
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
