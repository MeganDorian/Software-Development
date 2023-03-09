package org.itmo;

import org.itmo.exceptions.FlagNotFoundException;
import org.itmo.modules.Checker;
import org.itmo.modules.Executor;
import org.itmo.modules.Parser;
import org.itmo.modules.Reader;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Reader reader = new Reader();
        Parser parser = new Parser();
        Checker checker = new Checker();
        Executor executor = new Executor();
        List<CommandInfo> allCommands;
        do
        {
            String command = reader.readInput();
            allCommands = parser.commandParser(parser.substitutor(command).toString());
            try
            {
                checker.checkCommand(allCommands);
            } catch (FlagNotFoundException e)
            {
                e.printStackTrace();
                allCommands.clear();
            }
        } while (executor.run(allCommands));
        CommandResultSaver.deleteCommandResult();
    }
}