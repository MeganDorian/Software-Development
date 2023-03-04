package org.itmo.modules;

import java.io.IOException;
import java.io.InputStream;

public class Reader {
    
    /**
     * Symbol which represents end of the line
     */
    private final static String END_SYMBOL = "\n";
    private final InputStream inputStream;
    
    public Reader() {
        inputStream = System.in;
    }
    
    /**
     * Reads symbols from the input stream until meets END_SYMBOL
     */
    public String readInput() {
        StringBuilder line = new StringBuilder(100);
        try {
            String c = Character.toString(inputStream.read());
            while (!c.equals(END_SYMBOL)) {
                line.append(c);
                c = Character.toString(inputStream.read());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return line.toString().trim();
    }
    
}
