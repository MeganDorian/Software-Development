package org.itmo.commands.cat;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.modules.Reader;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.FileUtils;
import org.itmo.utils.command.CommandResultSaver;

/**
 * CAT command to work with file contents
 */
public class Cat implements Command {
    private final List<CatFlags> flags;
    private final List<String> params;
    
    public Cat(CommandInfo commandInfo) {
        flags = new ArrayList<>();
        commandInfo.getFlags().forEach(
            flag -> flags.add(CatFlags.valueOf(flag.replaceAll("^-{1,2}", "").toUpperCase())));
        params = commandInfo.getParams();
    }
    
    /**
     * If no parameters were passed to cat - reads one line from input stream otherwise - reads
     * content from all passed parameters if they are file
     *
     * @throws CatFileNotFoundException if parameter is not file
     */
    @Override
    public void execute() throws CatFileNotFoundException, IOException {
        if (printHelp()) {
            return;
        }
        
        if (!params.isEmpty()) {
            readContentFromFiles();
        }
        // read from input stream
        else if (CommandResultSaver.getInputStream().available() <= 0) {
            writeToOutput(new Reader().readInput().get(), 1);
        }
        // read from input stream and save it
        else {
            readFromInputStream(CommandResultSaver.getInputStream(), 1);
        }
    }
    
    private int writeToOutput(String l, int lineNumber) throws IOException {
        String line = appendNumberOfLines(lineNumber) + l + appendDollarSymbol() + "\n";
        CommandResultSaver.writeToOutput(line, APPEND_TO_OUTPUT);
        return lineNumber + 1;
    }
    
    private void readContentFromFiles() throws CatFileNotFoundException, IOException {
        int lineNumber = 1;
        for (String fileName : params) {
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                throw new CatFileNotFoundException(
                    "Cat command did not found file with name " + fileName);
            }
            lineNumber = readFromInputStream(FileUtils.getFileAsStream(fileName), lineNumber);
        }
    }
    
    private int readFromInputStream(InputStream stream, int lineNumber) throws IOException {
        try (BufferedReader reader = getReader(stream)) {
            while (reader.ready()) {
                String line = reader.readLine();
                lineNumber = writeToOutput(line, lineNumber);
            }
        }
        return lineNumber;
    }
    
    private String appendNumberOfLines(Integer lineNumber) {
        return flags.contains(CatFlags.N) ? "\t" + lineNumber + "\t\t" : "";
    }
    
    private String appendDollarSymbol() {
        return flags.contains(CatFlags.E) ? "$" : "";
    }
    
    @Override
    public boolean printHelp() throws IOException {
        if (!flags.isEmpty() && (flags.contains(CatFlags.HELP) || flags.contains(CatFlags.H))) {
            print(Commands.cat);
            return true;
        }
        return false;
    }
}