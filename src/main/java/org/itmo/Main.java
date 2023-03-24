package org.itmo;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.itmo.exceptions.CheckerException;
import org.itmo.exceptions.FlagNotFoundException;
import org.itmo.modules.Checker;
import org.itmo.modules.Parser;
import org.itmo.modules.Reader;
import org.itmo.modules.executor.Executor;
import org.itmo.modules.executor.ExecutorFlags;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.command.CommandResultSaver;

public class Main {
    public static void main(String[] args) {
        Reader reader = new Reader();
        Parser parser = new Parser();
        Checker checker = new Checker();
        Executor executor = new Executor();
        List<CommandInfo> allCommands;
        do {
            System.out.print(">> ");
            Optional<String> command = reader.readInput();
            if (!command.isPresent()) {
                return;
            }
            allCommands = parser.commandParser(parser.substitutor(command.get()));
            try {
                checker.checkCommand(allCommands);
            } catch (FlagNotFoundException | CheckerException e) {
                System.out.println(e.getCause().getMessage());
                allCommands.clear();
            }
        } while (executor.run(allCommands) != ExecutorFlags.EXIT);
        
        try {
            CommandResultSaver.closeStreams();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}