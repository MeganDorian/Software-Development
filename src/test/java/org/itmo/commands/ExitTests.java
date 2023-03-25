package org.itmo.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.itmo.commands.exit.Exit;
import org.itmo.utils.command.CommandResultSaver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExitTests {
    
    @BeforeEach
    public void setUp() {
        CommandResultSaver.initStreams();
    }
    
    @Test
    public void shouldExit() {
        Exit exit = new Exit();
        assertDoesNotThrow(exit::execute);
        assertEquals(0, CommandResultSaver.getInputStream().available());
        assertEquals(0, CommandResultSaver.getOutputStream().size());
    }
    
    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.closeStreams();
    }
}