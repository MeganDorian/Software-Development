package org.itmo.modules;

import org.itmo.commands.Commands;
import org.itmo.commands.cat.CatFlags;
import org.itmo.commands.pwd.PwdFlags;
import org.itmo.commands.wc.WcFlags;
import org.itmo.exceptions.CheckerException;
import org.itmo.exceptions.FlagNotFoundException;
import org.itmo.utils.CommandInfo;

import java.util.List;

/**
 * Check commands and their flags
 */
public class Checker {
    
    /**
     * Check commands
     *
     * @param command - information about command
     * @return true if the command is valid, false otherwise
     */
    public boolean checkCommand(List<CommandInfo> command) throws FlagNotFoundException, CheckerException
    {
        for (CommandInfo com : command) {
            try {
                Commands.valueOf(com.getCommandName());
                switch (com.getCommandName()) {
                    case "cat": {
                        for (int i = 0; i < com.getFlags().size(); i++) {
                            if (!CatFlags.isBelongs(com.getFlags().get(i))) {
                                throw new FlagNotFoundException("cat: unrecognized option '"
                                        + com.getParams().get(i)
                                        + "'\nTry 'cat -h' for more information.");
                            }
                        }
                        break;
                    }
                    case "pwd": {
                        for (int i = 0; i < com.getFlags().size(); i++) {
                            if (!PwdFlags.isBelongs(com.getFlags().get(i))) {
                                throw new FlagNotFoundException("pwd: unrecognized option '"
                                        + com.getParams().get(i)
                                        + "'\nTry 'pwd -h' for more information.");
                            }
                        }
                        break;
                    }
                    case "wc": {
                        for (int i = 0; i < com.getFlags().size(); i++) {
                            if (!WcFlags.isBelongs(com.getFlags().get(i))) {
                                throw new FlagNotFoundException("wc: unrecognized option '"
                                        + com.getParams().get(i)
                                        + "'\nTry 'wc -h' for more information.");
                            }
                        }
                        break;
                    }
                }
            } catch (IllegalArgumentException ignored) {
            
            } catch (Exception exception) {
                throw new CheckerException(exception);
            }
        }
        return true;
    }
}
