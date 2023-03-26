package org.itmo.modules.parser;

import com.beust.jcommander.JCommander;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.commands.cat.Cat;
import org.itmo.commands.echo.Echo;
import org.itmo.commands.exit.Exit;
import org.itmo.commands.external.External;
import org.itmo.commands.grep.Grep;
import org.itmo.commands.pwd.Pwd;
import org.itmo.commands.wc.Wc;
import org.itmo.modules.LocalStorage;


@AllArgsConstructor
public class CommandParser {
    
    private final Pattern variableAddition = Pattern.compile("^[^ ]+=[^ ]*");
    private List<String> parsedCommands;
    private LocalStorage localStorage;
    
    /**
     * Initialises JCommander object which can parse the passed command. For internal commands uses
     * addCommand() method, cause no need of the command name.
     * <p>
     * For the external commands uses addObject() method as for external command execution needs its
     * name
     *
     * @param commandName name of command
     * @param command     command object to parser mapping
     *
     * @return parser
     */
    private JCommander initCommander(String commandName, Command command) {
        JCommander jc;
        if (isInternal(commandName)) {
            jc = JCommander.newBuilder().addCommand(commandName, command).build();
            
        } else {
            jc = JCommander.newBuilder().addObject(command).build();
        }
        jc.setCaseSensitiveOptions(false);
        return jc;
    }
    
    
    /**
     * Tries to match variable from the passed string and save it to the local storage
     *
     * @param parsedCommand string to parse
     *
     * @return true if variable was found in the string and saved to the local storage
     */
    private boolean matchVariable(String parsedCommand) {
        Matcher matcherVariableAddition = variableAddition.matcher(parsedCommand);
        if (!matcherVariableAddition.find()) {
            return false;
        }
        int indexEq = parsedCommand.indexOf("=");
        localStorage.set(parsedCommand.substring(0, indexEq), parsedCommand.substring(indexEq + 1));
        return true;
    }
    
    /**
     * Calls the corresponding constructor based on the command name and return created object <br>
     *
     * @param commandName name of command
     *
     * @return created command object
     */
    private Command createCommandByName(String commandName) {
        Commands commands = getCommandName(commandName);
        switch (commands) {
            case cat: {
                return new Cat();
            }
            case echo: {
                return new Echo();
            }
            case pwd: {
                return new Pwd();
            }
            case wc: {
                return new Wc();
            }
            case grep:
                return new Grep();
            case exit:
                return new Exit();
            default:
                return new External();
        }
    }
    
    /**
     * @param commandName string representation of command name
     *
     * @return enum representation of command
     */
    private Commands getCommandName(String commandName) {
        if (!isInternal(commandName)) {
            return Commands.external;
        }
        return Commands.valueOf(commandName);
    }
    
    /**
     * Checks whether the command is internal
     *
     * @param commandName -- command name to check
     *
     * @return <true> -- if the command is internal, <false> -- if the command is external
     */
    private boolean isInternal(String commandName) {
        try {
            Commands.valueOf(commandName);
            return true;
        } catch (IllegalArgumentException ignored) {
            //if it is an external command
            return false;
        }
    }
    
    
    /**
     * Parses the string into commands
     * <p>
     * If the command is a variable initialisation/reinitialisation, it performs this
     *
     * @return command name, flags and parameters if it is a command and an empty list if it is a
     * variable initialisation/reinitialisation
     */
    public List<Command> commandParser() {
        List<Command> commands = new ArrayList<>();
        for (String parsedCommand : parsedCommands) {
            if (matchVariable(parsedCommand)) {
                continue;
            }
            int index = parsedCommand.indexOf(" ");
            String commandName =
                parsedCommand.substring(0, index != -1 ? index : parsedCommand.length());
            Command command = createCommandByName(commandName);
            JCommander jc = initCommander(commandName, command);
            if (!commandName.equals("echo") && isInternal(commandName)) {
                parsedCommand = parsedCommand.replaceAll(" +", " ");
            }
            jc.parse(parsedCommand.split(" "));
            commands.add(command);
        }
        return commands;
    }
}
