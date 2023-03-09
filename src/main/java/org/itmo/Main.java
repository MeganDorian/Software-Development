package org.itmo;

import org.itmo.modules.Reader;

public class Main {
    public static void main(String[] args) {
        Reader reader = new Reader();
        
        String command = reader.readInput();
        // parsed = Parse(command)
        // checked = Checker(parsed)
        // continue = Executor(checked)
        // in executor add creation of temporary file
    }
}