package org.itmo.commands;

import org.itmo.commands.exit.Exit;
import org.itmo.utils.CommandResultSaver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExitTests {
    
    @BeforeEach
    public void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
    }
    
    @Test
    public void shouldExitAndDeleteFile() {
        File file = CommandResultSaver.getResult().toFile();
        
        Exit exit = new Exit();
        assertDoesNotThrow(() -> exit.execute());
        
        assertFalse(file.exists());
    }
}