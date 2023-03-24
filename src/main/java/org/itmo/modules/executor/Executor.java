package org.itmo.modules.executor;

import static org.itmo.commands.Commands.exit;
import static org.itmo.modules.executor.ExecutorFlags.CONTINUE;
import static org.itmo.modules.executor.ExecutorFlags.EXIT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.itmo.commands.Command;
import org.itmo.commands.cat.Cat;
import org.itmo.commands.echo.Echo;
import org.itmo.commands.exit.Exit;
import org.itmo.commands.external.External;
import org.itmo.commands.pwd.Pwd;
import org.itmo.commands.wc.Wc;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.exceptions.ExternalException;
import org.itmo.exceptions.WcFileNotFoundException;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.command.CommandResultSaver;

public class Executor {
    
    private ExecutorFlags flag = CONTINUE;
    
    private boolean isNeedToPrintResult = true;
    
    public Executor() {
        CommandResultSaver.initStreams();
    }
    
    /**
     * Calls the corresponding constructor based on the command name and return created object <br>
     * creates an instance of: <br> - cat; <br> - wc; <br> - echo; <br> - pwd; <br> - external.
     * <br>
     *
     * @param commandInfo information about command
     *
     * @return created command object
     */
    private Command build(CommandInfo commandInfo) {
        isNeedToPrintResult = true;
        switch (commandInfo.getCommandName()) {
            case cat: {
                return new Cat(commandInfo);
            }
            case echo: {
                return new Echo(commandInfo);
            }
            case pwd: {
                return new Pwd(commandInfo);
            }
            case wc: {
                return new Wc(commandInfo);
            }
            default: {
                isNeedToPrintResult = false; // will be false if the last command was external
                return new External(commandInfo);
            }
        }
    }
    
    /**
     * Executes every command from the parsed list of commands
     *
     * @param allCommands the list of commands to execute
     *
     * @throws Exception if the command threw exception
     */
    private void executeEachCommand(List<CommandInfo> allCommands) throws Exception {
        if (allCommands.isEmpty()) {
            flag = CONTINUE;
        }
        for (CommandInfo commandInfo : allCommands) {
            if (commandInfo.getCommandName() == exit) {
                new Exit().execute();
                flag = EXIT;
                isNeedToPrintResult = false;
                return;
            }
            Command command = build(commandInfo);
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
    
    public ExecutorFlags run(List<CommandInfo> allCommands) {
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
