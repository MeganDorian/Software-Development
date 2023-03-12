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
import org.itmo.utils.FileInfo;
import org.itmo.utils.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Executor {
    
    public Executor() {
        try {
            CommandResultSaver.createCommandResultFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean run(List<CommandInfo> allCommands) {
        try {
            CommandResultSaver.clearCommandResult();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!allCommands.isEmpty()) {
            try {
                for (CommandInfo command : allCommands) {
                    switch (command.getCommandName()) {
                        case "cat": {
                            Cat cat = new Cat(command);
                            cat.execute();
                            break;
                        }
                        case "echo": {
                            Echo echo = new Echo(command);
                            echo.execute();
                            break;
                        }
                        case "exit": {
                            Exit exit = new Exit();
                            exit.execute();
                            return false;
                        }
                        case "pwd": {
                            Pwd pwd = new Pwd(command);
                            pwd.execute();
                            break;
                        }
                        case "wc": {
                            Wc wc = new Wc(command);
                            wc.execute();
                            break;
                        }
                        default: {
                            External external = new External(command);
                            external.execute();
                            break;
                        }
                    }
                }
            } catch (CatFileNotFoundException | WcFileNotFoundException | ExternalException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            FileInfo fInfo = FileUtils.getFileInfo(CommandResultSaver.getResult().toFile().getPath(), false);
            Optional<String> line = FileUtils.loadLineFromFile(fInfo);
            while (line.isPresent()) {
                System.out.println(line.get());
                line = FileUtils.loadLineFromFile(fInfo);
            }
        }
        return true;
    }
    
}
