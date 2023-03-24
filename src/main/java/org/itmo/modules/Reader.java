package org.itmo.modules;

import java.io.InputStream;
import java.util.Optional;
import java.util.Scanner;

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
    public Optional<String> readInput() {
        StringBuilder line = new StringBuilder(100);
        Scanner scan = new Scanner(inputStream);
        if (scan.hasNextLine()) {
            return Optional.of(line.append(scan.nextLine()).toString().trim());
        } else {
            return Optional.empty();
        }
    }
    
}
