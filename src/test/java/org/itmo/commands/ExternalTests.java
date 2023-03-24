package org.itmo.commands;

import org.itmo.commands.external.External;
import org.itmo.exceptions.ExternalException;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExternalTests {
    
    External external;
    
    @BeforeEach
    public void createFile() throws IOException {
        CommandResultSaver.createCommandResultFile();
    }
    
    @Test
    public void runExternalCommand() throws ExternalException {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            external = new External(new CommandInfo(Commands.external, List.of("cd"), new ArrayList<>()));
        } else {
            external = new External(new CommandInfo(Commands.external, List.of("pwd"), new ArrayList<>()));
        }
        external.execute();
        CommandResultSaver.saveCommandResult();
        String actual = FileUtils.loadFullContent(
                CommandResultSaver.getResult().toFile()
        ).replaceAll("\r", "").replaceAll("\n", "");
        assertEquals(System.getProperty("user.dir"), actual);
    }
    
    @Test
    public void errorExternalCommand() {
        external = new External(new CommandInfo(Commands.external, List.of("someCommand"), new ArrayList<>()));
        assertThrows(ExternalException.class, () -> external.execute());
    }
    
    @AfterEach
    public void cleanUp() {
        CommandResultSaver.deleteCommandResult();
    }
}
