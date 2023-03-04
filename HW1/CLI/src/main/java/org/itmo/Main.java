package org.itmo;

import org.itmo.modules.Reader;

public class Main {
    public static void main(String[] args) {
        Reader reader = new Reader();
        String line = reader.readInput(); // add answer from executor
        // send line to Parser
    }
}