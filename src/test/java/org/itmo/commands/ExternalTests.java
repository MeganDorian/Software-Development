package org.itmo.commands;

import org.itmo.commands.external.External;
import org.itmo.exceptions.ExternalException;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileInfo;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalTests {
    
    External external;
    
    @BeforeAll
    public static void createFile() throws IOException {
        CommandResultSaver.createCommandResultFile();
    }
    
    @Test
    public void runExternalCommand() throws ExternalException {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            external = new External(new CommandInfo("cd", new ArrayList<>(), new ArrayList<>()));
        }
        else {
            external = new External(new CommandInfo("pwd", new ArrayList<>(), new ArrayList<>()));
        }
        external.execute();
        FileInfo fInfo = FileUtils.getFileInfo(CommandResultSaver.getResult().toFile().getPath(), false);
        Optional<String> line = FileUtils.loadLineFromFile(fInfo);
        StringBuilder actual = new StringBuilder();
        while (line.isPresent()) {
            actual.append(line.get());
            line = FileUtils.loadLineFromFile(fInfo);
        }
        assertEquals(System.getProperty("user.dir"), actual.toString());
    }
    
    @Test
    public void errorExternalCommand() {
        external = new External(new CommandInfo("someCommand", new ArrayList<>(), new ArrayList<>()));
        assertThrows(ExternalException.class, () -> external.execute());
    }
    
}
