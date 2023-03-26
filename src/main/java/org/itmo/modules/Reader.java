package org.itmo.modules;

import java.util.Optional;
import java.util.Scanner;

public class Reader {
    
    /**
     * Reads symbols from the input stream until meets END_SYMBOL
     */
    public Optional<String> readInput() {
        StringBuilder line = new StringBuilder(100);
        Scanner scan = new Scanner(System.in);
        if (scan.hasNextLine()) {
            return Optional.of(line.append(scan.nextLine()).toString().trim());
        } else {
            return Optional.empty();
        }
    }
    
}
