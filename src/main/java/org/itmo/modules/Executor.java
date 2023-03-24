package org.itmo.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
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
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtils;

public class Executor {
    
    public Executor () {
        try {
            CommandResultSaver.createCommandResultFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean run (List<CommandInfo> allCommands) {
        try {
            CommandResultSaver.clearCommandResult();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean isInternal = true;
        if (!allCommands.isEmpty()) {
            try {
                for (CommandInfo command : allCommands) {
                    switch (command.getCommandName()) {
                        case cat: {
                            if (command.getParams().isEmpty()) {
                                command.addParams(CommandResultSaver.getResultPath());
                            }
                            Cat cat = new Cat(command);
                            cat.execute();
                            break;
                        }
                        case echo: {
                            Echo echo = new Echo(command);
                            echo.execute();
                            break;
                        }
                        case exit: {
                            Exit exit = new Exit();
                            exit.execute();
                            return false;
                        }
                        case pwd: {
                            Pwd pwd = new Pwd(command);
                            pwd.execute();
                            break;
                        }
                        case wc: {
                            if (command.getParams().isEmpty()) {
                                command.addParams(CommandResultSaver.getResultPath());
                            }
                            Wc wc = new Wc(command);
                            wc.execute();
                            break;
                        }
                        default: {
                            External external = new External(command);
                            external.execute();
                            isInternal = false;
                            break;
                        }
                    }
                    CommandResultSaver.saveCommandResult();
                    CommandResultSaver.clearPipeCommandResult();
                }
            } catch (CatFileNotFoundException | WcFileNotFoundException | ExternalException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (allCommands.size() > 1 || (allCommands.size() == 1 && isInternal)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    FileUtils.getFileAsStream(CommandResultSaver.getResultPath())))) {
                    while (reader.ready()) {
                        System.out.println(reader.readLine());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }
    
}
