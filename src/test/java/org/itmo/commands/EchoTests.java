package org.itmo.commands;

import static org.itmo.commands.Commands.echo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.itmo.commands.echo.Echo;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EchoTests {
    @BeforeEach
    public void setUp() throws IOException {
        CommandResultSaver.createCommandResultFile();
        CommandResultSaver.savePipeCommandResult("");
    }
    
    @Test
    public void shouldPrintValues() {
        CommandInfo info =
            new CommandInfo(echo, Collections.emptyList(), List.of("Obi", "Wan", "Kenobi"));
        checkResult("Obi Wan Kenobi", info);
    }
    
    private void checkResult(String expected, CommandInfo info) {
        Echo echo = new Echo(info);
        assertDoesNotThrow(echo::execute);
        CommandResultSaver.saveCommandResult();
        String actual =
            FileUtils.loadFullContent(CommandResultSaver.getResult().toFile()).replace("\r", "")
                .replace("\n", "");
        assertEquals(expected, actual);
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
