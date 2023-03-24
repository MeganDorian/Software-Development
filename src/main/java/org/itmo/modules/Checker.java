package org.itmo.modules;

import java.util.List;
import org.itmo.commands.Commands;
import org.itmo.commands.cat.CatFlags;
import org.itmo.commands.pwd.PwdFlags;
import org.itmo.commands.wc.WcFlags;
import org.itmo.exceptions.CheckerException;
import org.itmo.exceptions.FlagNotFoundException;
import org.itmo.utils.CommandInfo;

/**
 * Check commands and their flags
 */
public class Checker {
    
    /**
     * Checks whether the command is internal
     *
     * @param commandName -- command name to check
     *
     * @return <true> -- if the command is internal, <false> -- if the command is external
     */
    public static boolean checkCommandIsInternal(String commandName) {
        try {
            Commands.valueOf(commandName);
            return true;
        } catch (IllegalArgumentException ignored) {
            //if it is an external command
            return false;
        }
    }
    
    /**
     * Check commands
     *
     * @param command - information about command
     */
    public void checkCommand(List<CommandInfo> command)
        throws FlagNotFoundException, CheckerException {
        for (CommandInfo com : command) {
            try {
                switch (com.getCommandName()) {
                    case cat: {
                        for (int i = 0; i < com.getFlags().size(); i++) {
                            if (!CatFlags.isBelongs(com.getFlags().get(i))) {
                                throw new FlagNotFoundException(
                                    "cat: unrecognized option '" + com.getFlags().get(i) +
                                    "'\nTry 'cat --help' for more information.");
                            }
                        }
                        break;
                    }
                    case pwd: {
                        for (int i = 0; i < com.getFlags().size(); i++) {
                            if (!PwdFlags.isBelongs(com.getFlags().get(i))) {
                                throw new FlagNotFoundException(
                                    "pwd: unrecognized option '" + com.getFlags().get(i) +
                                    "'\nTry 'pwd --help' for more information.");
                            }
                        }
                        break;
                    }
                    case wc: {
                        for (int i = 0; i < com.getFlags().size(); i++) {
                            if (!WcFlags.isBelongs(com.getFlags().get(i))) {
                                throw new FlagNotFoundException(
                                    "wc: unrecognized option '" + com.getFlags().get(i) +
                                    "'\nTry 'wc --help' for more information.");
                            }
                        }
                        break;
                    }
                }
            } catch (IllegalArgumentException ignored) {
                //if it is an external command
            } catch (FlagNotFoundException exception) {
                //if the flag for the embedded command is incorrect
                throw new FlagNotFoundException(exception);
            } catch (Exception exception) {
                throw new CheckerException(exception);
            }
        }
    }
    
}
