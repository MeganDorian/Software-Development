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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ExternalTests {
    
    External external;
    
    @BeforeAll
    public static void createFile() {
        try
        {
            CommandResultSaver.createCommandResultFile();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void runExternalCommand() {
        try
        {
            if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
            {
                external = new External(new CommandInfo("cd", new ArrayList<>(), new ArrayList<>()));
            } else {
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
        } catch (Exception exception) {
            fail("Unexpected error: " + exception.getCause().getMessage());
        }
    }
    
    @Test
    public void errorExternalCommand() {
        try
        {
            external = new External(new CommandInfo("someCommand", new ArrayList<>(), new ArrayList<>()));
            external.execute();
        } catch (ExternalException exception) {
            return;
        }
        fail("No exception");
    }
    
}
