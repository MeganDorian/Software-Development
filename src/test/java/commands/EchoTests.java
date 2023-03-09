package commands;

import org.itmo.commands.echo.Echo;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EchoTests {
    
    @Test
    public void shouldPrintValues() {
        List<String> values = List.of("asfg", "sdkfj", "sdlkgjsd;jgl");
        CommandInfo info = new CommandInfo("echo", Collections.emptyList(), values);
        
        Echo echo = new Echo(info);
        echo.execute();
    
        String expected = "asfg sdkfj sdlkgjsd;jgl";
        String actual = FileUtils.loadFullContent(CommandResultSaver.getResult().toFile()).replace("\r", "");
        
        assertEquals(expected, actual);
    }
    
    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.clearCommandResult();
    }
}
