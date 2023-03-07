package commands;

import org.itmo.commands.cat.Cat;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.utils.CommandInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CatTests {
    private CommandInfo info;
    
    @BeforeEach
    public void setup() {
        info = mock(CommandInfo.class);
        when(info.getCommandName()).thenReturn("cat");
    }
    @Test
    public void shouldReadFromInputStream() {
        when(info.getFlags()).thenReturn(Collections.emptyList());
        when(info.getParams()).thenReturn(Collections.emptyList());
        Cat cat = new Cat(info);
        assertDoesNotThrow(cat::execute);
    }
    
    @Test
    public void shouldPrintHelp() throws CatFileNotFoundException {
        String help = """
                Usage: cat [OPTION]... [FILE]...
                Concatenate FILE(s) to standard output.
        
                With no FILE read standard input.
        
                    -e              - display $ at end of each line
                    -n              - number all output lines
                    --h, --help     - display this help and exit
                """;
        when(info.getFlags()).thenReturn(List.of("--help"));
        when(info.getParams()).thenReturn(Collections.emptyList());
        
        Cat cat = new Cat(info);
        cat.execute();
    
//        ResourcesLoader.getFileFromResource()
    }
}
