package org.itmo.commands.grep;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.GrepException;
import org.itmo.modules.Reader;
import org.itmo.utils.FileUtils;
import org.itmo.utils.command.CommandResultSaver;

/**
 * GREP command to search by regex in text
 */
@Parameters(commandDescription = "GREP command to search by regex in text")
@AllArgsConstructor
@NoArgsConstructor
public class Grep implements Command {
    @Getter
    @Parameter(names = "-w", description = "searches the whole word to match")
    private boolean searchFullWord;
    @Getter
    @Parameter(names = "-i", description = "case-insensitive search")
    private boolean caseInsensitive;
    @Getter
    @Parameter(names = "-A", description = "how many lines after the match need to print")
    private Integer lineCountToPrint = 0;
    @Getter
    @Parameter(description = "The list of files to search in")
    private List<String> patternAndFiles = new ArrayList<>();
    
    /**
     * Executes grep command
     *
     * @throws GrepException if no pattern passed to the command or file to search not found
     * @throws IOException   if unable to write to the common output stream
     */
    @Override
    public void execute() throws GrepException, IOException {
        if (patternAndFiles.isEmpty()) {
            throw new GrepException("You need to specify pattern to match");
        }
        
        String stringPattern = patternAndFiles.get(0);
        patternAndFiles.remove(0);
        
        if (isSearchFullWord()) {
            stringPattern = "\\b" + stringPattern + "\\b";
        }
        
        if (isCaseInsensitive()) {
            stringPattern = stringPattern.toLowerCase(Locale.ROOT);
        }
        
        Pattern pattern = Pattern.compile(stringPattern);
        
        if (!patternAndFiles.isEmpty()) {
            searchInFiles(pattern);
        }
        // read from input stream
        else if (CommandResultSaver.getInputStream().available() <= 0) {
            String line = new Reader().readInput().get();
            if (checkPattern(pattern, line)) {
                writeToOutput(line);
            }
        }
        // read from input stream
        else {
            readFromInputStream(CommandResultSaver.getInputStream(), pattern);
        }
    }
    
    /**
     * Write to the common output stream
     *
     * @param line string to write to stream
     *
     * @throws IOException if unable to write to the common output stream
     */
    private void writeToOutput(String line) throws IOException {
        CommandResultSaver.writeToOutput(line + "\n", APPEND_TO_OUTPUT);
    }
    
    /**
     * In cycle reads content from each file from the list of files and searches in their content
     * pattern. Found result writes to the output stream
     *
     * @param pattern to search
     *
     * @throws GrepException if file to search not found
     */
    private void searchInFiles(Pattern pattern) throws GrepException {
        for (String fileName : patternAndFiles) {
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                throw new GrepException("grep command did not found with  name " + fileName);
            }
            readFromInputStream(FileUtils.getFileAsStream(fileName), pattern);
        }
    }
    
    /**
     * Check id the line matches pattern
     *
     * @param pattern to search
     * @param line    to match
     *
     * @return true if found pattern in the line.
     * <p>
     * false otherwise
     */
    private boolean checkPattern(Pattern pattern, String line) {
        line = isCaseInsensitive() ? line.toLowerCase(Locale.ROOT) : line;
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }
    
    /**
     * Opens the input stream (file or system input or common input stream) and reads content from
     * it. Then searches for pattern and writes to the common output stream found lines. If pattern
     * found and lineCountToPrint not equal to zero, writes next line to the common output stream
     *
     * @param inputStream stream to read from
     * @param pattern     to search
     */
    private void readFromInputStream(InputStream inputStream, Pattern pattern) {
        int lineCount = getLineCountToPrint();
        boolean found = false;
        try (BufferedReader reader = getReader(inputStream)) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (checkPattern(pattern, line)) {
                    lineCount = getLineCountToPrint();
                    writeToOutput(line);
                    found = true;
                } else {
                    --lineCount;
                    if (lineCount >= 0 && found) {
                        writeToOutput(line);
                    } else {
                        found = false;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean printHelp() {
        return false;
    }
    
    @Override
    public Commands getCommandName() {
        return Commands.grep;
    }
}
