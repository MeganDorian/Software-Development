package modules;

import org.itmo.modules.Reader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ReaderTests {
    
    @Test
    public void shouldReadFromConsole() {
        InputStream forTests;
        String generatedString = "test";
        forTests = new ByteArrayInputStream((generatedString + "\n").getBytes());
        System.setIn(forTests);
        
        Reader reader = new Reader();
        String result = reader.readInput();
        assertEquals(generatedString, result);
    }
}
