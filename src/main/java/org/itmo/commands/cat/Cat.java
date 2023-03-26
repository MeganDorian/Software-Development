package org.itmo.commands.cat;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.modules.Reader;
import org.itmo.utils.FileUtils;
import org.itmo.utils.command.CommandResultSaver;

/**
 * CAT command to work with file contents
 */
@Parameters(
    commandDescription = "Concatenate FILE(s) to standard output. With no FILE read standard input.")
@AllArgsConstructor
@NoArgsConstructor
public class Cat implements Command {
    
    @Getter
    @Parameter(names = {"--E", "-e"}, description = "display $ at end of each line")
    private boolean displayDollar;
    @Getter
    @Parameter(names = {"--N", "-n"}, description = "number all output lines")
    private boolean numberOfLine;
    @Getter
    @Parameter(names = {"--help", "-h"}, description = "display this help and exit", help = true)
    private boolean help;
    @Getter
    @Parameter(description = "list of files to output")
    private List<String> files;
    
    
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
        
        if (!files.isEmpty()) {
            readContentFromFiles();
        }
        // read from system input stream
        else if (CommandResultSaver.getInputStream().available() <= 0) {
            writeToOutput(new Reader().readInput().get(), 1);
        }
        // read from input stream and save it
        else {
            readFromInputStream(CommandResultSaver.getInputStream(), 1);
        }
    }
    
    /**
     * Write to the common output stream
     *
     * @param l          string to write to stream
     * @param lineNumber number of line append if the flag is presented
     *
     * @return number of line
     *
     * @throws IOException if unable to write to the common output stream
     */
    private int writeToOutput(String l, int lineNumber) throws IOException {
        String line = appendNumberOfLines(lineNumber) + l + appendDollarSymbol() + "\n";
        CommandResultSaver.writeToOutput(line, APPEND_TO_OUTPUT);
        return lineNumber + 1;
    }
    
    /**
     * In cycle reads content from each file from the list of files and writes their content to the
     * output stream with dollar symbol or line number if needed
     *
     * @throws CatFileNotFoundException if can't find file from the list
     * @throws IOException              if unable to write to the common output stream
     */
    private void readContentFromFiles() throws CatFileNotFoundException, IOException {
        int lineNumber = 1;
        for (String fileName : files) {
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                throw new CatFileNotFoundException(
                    "Cat command did not found file with name " + fileName);
            }
            lineNumber = readFromInputStream(FileUtils.getFileAsStream(fileName), lineNumber);
        }
    }
    
    /**
     * Reads from input stream a line and writes it to the output stream with dollar symbol or line
     * number if needed
     *
     * @param stream     input stream to read from
     * @param lineNumber number of line append if the flag is presented
     *
     * @return number of line
     *
     * @throws IOException if unable to write to the common output stream
     */
    private int readFromInputStream(InputStream stream, int lineNumber) throws IOException {
        try (BufferedReader reader = getReader(stream)) {
            while (reader.ready()) {
                String line = reader.readLine();
                lineNumber = writeToOutput(line, lineNumber);
            }
        }
        return lineNumber;
    }
    
    /**
     * If flag -n is presented appends tabs and line number. Otherwise, return empty string
     *
     * @param lineNumber line number to append to the result if needed
     *
     * @return string with appended or not line number
     */
    private String appendNumberOfLines(Integer lineNumber) {
        return numberOfLine ? "\t" + lineNumber + "\t\t" : "";
    }
    
    /**
     * If flag -e is presented return dollar symbol. Otherwise, return empty string
     *
     * @return dollar symbol or empty string
     */
    private String appendDollarSymbol() {
        return displayDollar ? "$" : "";
    }
    
    @Override
    public boolean printHelp() throws IOException {
        if (help) {
            print(getCommandName());
            return true;
        }
        return false;
    }
    
    @Override
    public Commands getCommandName() {
        return Commands.cat;
    }
}