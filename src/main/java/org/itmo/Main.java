package org.itmo;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.itmo.commands.Command;
import org.itmo.modules.Reader;
import org.itmo.modules.executor.Executor;
import org.itmo.modules.executor.ExecutorFlags;
import org.itmo.modules.parser.Parser;
import org.itmo.utils.command.CommandResultSaver;

public class Main {
    public static void main(String[] args) {
        Reader reader = new Reader();
        Parser parser = new Parser();
        Executor executor = new Executor();
        List<Command> allCommands;
        do {
            System.out.print(">> ");
            Optional<String> command = reader.readInput();
            if (!command.isPresent()) {
                return;
            }
            allCommands = parser.commandParser(parser.substitutor(command.get()));
        } while (executor.run(allCommands) != ExecutorFlags.EXIT);
        
        try {
            CommandResultSaver.closeStreams();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}