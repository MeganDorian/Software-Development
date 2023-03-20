package org.itmo.modules;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ReaderTests {
    
    @Test
    public void shouldReadFromConsole() {
        String generatedString = "test";
        InputStream forTests = new ByteArrayInputStream((generatedString + "\n").getBytes());
        System.setIn(forTests);
        
        Reader reader = new Reader();
        String result = reader.readInput();
        assertEquals(generatedString, result);
    }
}
