package org.itmo.modules.executor;

import static org.itmo.commands.Commands.exit;
import static org.itmo.modules.executor.ExecutorFlags.CONTINUE;
import static org.itmo.modules.executor.ExecutorFlags.EXIT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.itmo.commands.Command;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.exceptions.ExternalException;
import org.itmo.exceptions.WcFileNotFoundException;
import org.itmo.utils.command.CommandResultSaver;

public class Executor {
    
    private ExecutorFlags flag = CONTINUE;
    
    private boolean isNeedToPrintResult = true;
    
    public Executor() {
        CommandResultSaver.initStreams();
    }
    
    /**
     * Executes every command from the parsed list of commands
     *
     * @param allCommands the list of commands to execute
     *
     * @throws Exception if the command threw exception
     */
    private void executeEachCommand(List<Command> allCommands) throws Exception {
        if (allCommands.isEmpty()) {
            flag = CONTINUE;
        }
        for (Command command : allCommands) {
            if (command.getCommandName() == exit) {
                command.execute();
                flag = EXIT;
                isNeedToPrintResult = false;
                return;
            }
            command.execute();
            CommandResultSaver.writeToInput();
            CommandResultSaver.clearOutput();
        }
        flag = CONTINUE;
    }
    
    /**
     * Loads the result of commands execution from the temporary file and prints it to the system
     * output stream
     */
    private void loadAndPrintCommandResult() throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(CommandResultSaver.getInputStream()))) {
            while (reader.ready()) {
                System.out.println(reader.readLine());
            }
        }
    }
    
    public ExecutorFlags run(List<Command> allCommands) {
        try {
            CommandResultSaver.clearOutput();
            executeEachCommand(allCommands);
            if (isNeedToPrintResult) {
                loadAndPrintCommandResult();
            }
        } catch (CatFileNotFoundException | WcFileNotFoundException | ExternalException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT;
        }
        return flag;
    }
    
}
