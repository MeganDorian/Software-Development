package org.itmo.commands.cat;

import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.modules.Reader;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.LoadHelp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * CAT command to work with file contents
 */
public class Cat implements Command {
    private List<CatFlags> flags;
    private final List<String> params;
    
    public Cat(CommandInfo commandInfo) {
        flags = Collections.emptyList();
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
            LoadHelp.printHelp(String.valueOf(Commands.cat));
            return;
        }
        
        StringBuilder line = new StringBuilder(100);
        boolean addNumber = flags.contains(CatFlags.E);
        boolean addDollarSymbol = flags.contains(CatFlags.E);
        int lineNumber = 1;
        
        if (addNumber) {
            line.append("\t").append(lineNumber).append("\t\t");
            lineNumber++;
        }
        if (addDollarSymbol) {
            line.append("$");
        }
        
        if (params.isEmpty()) {
            line.append(new Reader().readInput());
            CommandResultSaver.saveCommandResult(line.toString());
        } else {
            
            for (String fileName : params) {
                File file = new File(fileName);
                if (file.exists() && file.isFile()) {
                    throw new CatFileNotFoundException("File not found with name " + fileName);
                }
                try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                    String read = reader.readLine();
                    while (read != null) {
                        if (addNumber) {
                            line.append("\t").append(lineNumber).append("\t\t");
                            lineNumber++;
                        }
                        if (addDollarSymbol) {
                            line.append("$");
                        }
                        read = reader.readLine();
                        CommandResultSaver.saveCommandResult(line.toString());
                        line.delete(0, line.length());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}