package org.itmo.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import org.itmo.commands.external.External;
import org.itmo.exceptions.ExternalException;
import org.itmo.utils.command.CommandResultSaver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExternalTests {
    
    @BeforeEach
    public void createFile() {
        CommandResultSaver.initStreams();
    }
    
    @Test
    public void runExternalCommand() throws ExternalException, IOException {
        External external;
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            external = new External(List.of("cd"), true);
        } else {
            external = new External(List.of("pwd"), false);
        }
        external.execute();
        String actual =
            new String(CommandResultSaver.getOutputStream().toByteArray()).replaceAll("\r", "")
                                                                          .replaceAll("\n", "");
        
        assertEquals(System.getProperty("user.dir"), actual);
    }
    
    @Test
    public void errorExternalCommand() {
        External external = new External(List.of("someCommand"), false);
        assertThrows(ExternalException.class, external::execute);
    }
    
    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.closeStreams();
    }
}
