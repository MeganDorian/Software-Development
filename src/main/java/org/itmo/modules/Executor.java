package org.itmo.modules;

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

import java.io.IOException;
import java.util.List;

public class Executor {
    
    List<CommandInfo> allCommands;
    
    public Executor(List<CommandInfo> commands) {
        allCommands = commands;
        try {
            CommandResultSaver.createCommandResultFile();
        } catch (IOException e) {
            allCommands.clear();
            throw new RuntimeException(e);
        }
    }
    
    public boolean run() {
        if(!allCommands.isEmpty()) {
            try {
                for (CommandInfo command: allCommands) {
                    switch (command.getCommandName()) {
                        case "cat" -> {
                            Cat cat = new Cat(command);
                            cat.execute();
                        }
                        case "echo" -> {
                            Echo echo = new Echo(command);
                            echo.execute();
                        }
                        case "exit" -> {
                            Exit exit = new Exit();
                            exit.execute();
                            return false;
                        }
                        case "pwd" -> {
                            Pwd pwd = new Pwd(command);
                            pwd.execute();
                        }
                        case "wc" -> {
                            Wc wc = new Wc(command);
                            wc.execute();
                        }
                        default -> {
                            External external = new External(command);
                            external.execute();
                        }
                    }
                }
            } catch (CatFileNotFoundException | WcFileNotFoundException | ExternalException e)
            {
                e.printStackTrace();
            }  catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
            System.out.println(FileUtils.loadFullContent(CommandResultSaver.getResult().toFile()));
            CommandResultSaver.deleteCommandResult();
        }
        return true;
    }
    
}
