package org.itmo.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;


public class ReaderTests {
    
    @Test
    public void shouldReadFromConsole() {
        String generatedString = "test";
        InputStream forTests = new ByteArrayInputStream((generatedString + "\n").getBytes());
        System.setIn(forTests);
        
        Reader reader = new Reader();
        String result = reader.readInput().get();
        assertEquals(generatedString, result);
    }
}
