package org.itmo.modules;

import org.itmo.commands.Commands;
import org.itmo.commands.cat.CatFlags;
import org.itmo.commands.pwd.PwdFlags;
import org.itmo.utils.CommandInfo;

import java.io.File;
import java.util.Arrays;

/**
 * Check commands and their flags
 */
public class Checker {
    
    /**
     * Check commands
     * @param command - information about command
     * @return true if the command is valid, false otherwise
     */
    public boolean checkCommand(CommandInfo command) {
        if(Arrays.stream(Commands.values()).toList().contains(command.getCommandName())) {
            switch (command.getCommandName())
            {
                case "cat" ->
                {
                    for (int i = 0; i < command.getFlags().size(); i++)
                    {
                        if (!CatFlags.isBelongs(command.getParams().get(i)))
                        {
                            System.err.println("cat: unrecognized option '"
                                                       + command.getParams().get(i) + "'");
                            System.err.println("Try 'cat -h' for more information.");
                            return false;
                        }
                    }
                }
                case "pwd" ->
                {
                    for (int i = 0; i < command.getFlags().size(); i++)
                    {
                        if (!PwdFlags.isBelongs(command.getParams().get(i)))
                        {
                            System.err.println("pwd: unrecognized option '"
                                                       + command.getParams().get(i) + "'");
                            System.err.println("Try 'pwd -h' for more information.");
                            return false;
                        }
                    }
                }
                case "wc" ->
                {
//                    add with wc command
//                    for (int i = 0; i < command.getFlags().size(); i++)
//                    {
//                        if (!WcFlags.isBelongs(command.getParams().get(i)))
//                        {
//                            System.err.println("wc: unrecognized option '"
//                                                       + command.getParams().get(i) + "'");
//                            System.err.println("Try 'wc -h' for more information.");
//                            return false;
//                        }
//                    }
                }
            }
            
        } else {
            File f = new File(command.getCommandName());
            if(!f.exists() || f.isDirectory()) {
                System.err.println("Command '" + command.getCommandName() + "' not found");
            }
        }
        return true;
    }
}
