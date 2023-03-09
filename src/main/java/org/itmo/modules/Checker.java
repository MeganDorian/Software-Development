package org.itmo.modules;

import org.itmo.commands.Commands;
import org.itmo.commands.cat.CatFlags;
import org.itmo.commands.pwd.PwdFlags;
import org.itmo.exceptions.FlagNotFound;
import org.itmo.utils.CommandInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Check commands and their flags
 */
public class Checker {
    
    /**
     * Check commands
     * @param command - information about command
     * @return true if the command is valid, false otherwise
     */
    public boolean checkCommand(List<CommandInfo> command) throws FlagNotFound {
        for (CommandInfo com: command) {
            try  {
                Commands.valueOf(com.getCommandName());
                switch (com.getCommandName()) {
                    case "cat" -> {
                        for (int i = 0; i < com.getFlags().size(); i++) {
                            if (!CatFlags.isBelongs(com.getParams().get(i))) {
                                throw new FlagNotFound("cat: unrecognized option '"
                                                               + com.getParams().get(i)
                                                               + "'\nTry 'cat -h' for more information.");
                            }
                        }
                    }
                    case "pwd" -> {
                        for (int i = 0; i < com.getFlags().size(); i++) {
                            if (!PwdFlags.isBelongs(com.getParams().get(i))) {
                                throw new FlagNotFound("pwd: unrecognized option '"
                                                               + com.getParams().get(i)
                                                               + "'\nTry 'pwd -h' for more information.");
                            }
                        }
                    }
                    case "wc" -> {
                        //                    add with wc command
//                        for (int i = 0; i < com.getFlags().size(); i++) {
//                            if (!WcFlags.isBelongs(com.getParams().get(i))) {
//                                throw new FlagNotFound("wc: unrecognized option '"
//                                                               + com.getParams().get(i)
//                                                               + "'\nTry 'wc -h' for more information.");
//                            }
//                        }
                    }
                }
    
            } catch (IllegalArgumentException exception) {
                File f = new File(com.getCommandName());
                if (!f.exists() || f.isDirectory()) {
                    System.err.println("Command '" + com.getCommandName() + "' not found");
                }
            }
        }
        return true;
    }
}
