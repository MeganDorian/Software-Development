package org.itmo.commands.cat;

import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.modules.Reader;
import org.itmo.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CAT command to work with file contents
 */
public class Cat implements Command {
    private List<CatFlags> flags;
    private final List<String> params;
    
    public Cat(CommandInfo commandInfo) {
        flags = new ArrayList<>();
        commandInfo.getFlags().forEach(flag -> {
            flags.add(CatFlags.valueOf(flag.replaceAll("-", "").toUpperCase()));
        });
        params = commandInfo.getParams();
    }
    
    /**
     * If no parameters were passed to cat - reads one line from input stream
     * otherwise - reads content from all passed parameters if they are file
     *
     * @throws CatFileNotFoundException if parameter is not file
     */
    @Override
    public void execute() throws CatFileNotFoundException {
        if (!flags.isEmpty() && flags.contains(CatFlags.HELP) || flags.contains(CatFlags.H)) {
            FileInfo helpInfo = FileUtils.getFileInfo(ResourcesLoader.getProperty(Commands.cat + ".help"));
            while (helpInfo.getPosition() < helpInfo.getFileSize()) {
                Optional<String> line = FileUtils.loadLineFromFile(helpInfo);
                line.ifPresent( l -> CommandResultSaver.saveCommandResult(l, true));
            }
            return;
        }
        
        StringBuilder line = new StringBuilder();
        boolean addNumber = flags.contains(CatFlags.E);
        boolean addDollarSymbol = flags.contains(CatFlags.E);
        int lineNumber = 1;
        
        if (params.isEmpty()) {
            if (addNumber) {
                line.append("\t").append(lineNumber).append("\t\t");
            }
            line.append(new Reader().readInput());
            
            if (addDollarSymbol) {
                line.append("$");
            }
            CommandResultSaver.saveCommandResult(line.toString(), false);
        } else {
            
            for (String fileName : params) {
                File file = new File(fileName);
                if (!file.exists() || !file.isFile()) {
                    throw new CatFileNotFoundException("File not found with name " + fileName);
                }
                try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                    String l = reader.readLine();
                    while (l != null) {
                        if (addNumber) {
                            line.append("\t").append(lineNumber).append("\t\t");
                            lineNumber++;
                        }
                        line.append(l);
                        if (addDollarSymbol) {
                            line.append("$");
                        }
                        CommandResultSaver.saveCommandResult(line.toString(), true);
                        line.delete(0, line.length());
                        l = reader.readLine();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
}