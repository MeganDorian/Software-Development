package org.itmo.commands.wc;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.WcFileNotFoundException;
import org.itmo.modules.Reader;
import org.itmo.utils.FileUtils;
import org.itmo.utils.Pair;
import org.itmo.utils.command.CommandResultSaver;

/**
 * WC command to count
 */
@Parameters(commandDescription = "WC command to count")
@AllArgsConstructor
@NoArgsConstructor
public class Wc implements Command {
    
    @Parameter(names = {"--help", "-h"}, description = "display this help and exit", help = true)
    private boolean help;
    
    @Parameter(names = "-c", description = "byte count")
    private boolean byteCountOnly;
    
    @Parameter(names = "-l", description = "line count")
    private boolean lineCountOnly;
    
    @Parameter(names = "-w", description = "words count")
    private boolean wordsCountOnly;
    
    @Parameter(description = "files to count")
    private List<String> params;
    
    /**
     * Count of lines <br> first - count in for the current parameter <br> second - count in total
     */
    private final Pair<Long> lineCount = new Pair<>(0L, 0L);
    
    /**
     * Count of word <br> first - count in for the current parameter <br> second - count in total
     */
    private final Pair<Long> wordsCount = new Pair<>(0L, 0L);
    
    /**
     * Count of bytes <br> first - count in for the current parameter <br> second - count in total
     */
    private final Pair<Long> byteCount = new Pair<>(0L, 0L);
    
    /**
     * Counts lines, words, bytes count from the list of params. If no params, then reads from input
     * stream
     *
     * @throws WcFileNotFoundException if no file with passed name found
     * @throws IOException             if unable to write to the common output stream
     */
    @Override
    public void execute() throws WcFileNotFoundException, IOException {
        if (printHelp()) {
            return;
        }
        
        if (!params.isEmpty()) {
            countFromFiles();
            if (params.size() > 1) {
                write(lineCount.second, wordsCount.second, byteCount.second, "total");
            }
        }
        // read from input stream
        else if (CommandResultSaver.getInputStream().available() <= 0) {
            String line = new Reader().readInput().get();
            wordsCount.first = Arrays.stream(line.split(" ")).filter(v -> !v.equals("")).count();
            byteCount.first = (long) line.getBytes(StandardCharsets.UTF_8).length;
            write(1, wordsCount.first, byteCount.first, null);
        } else {
            readFromInputStream(CommandResultSaver.getInputStream(), null);
        }
    }
    
    /**
     * Reads from passed input stream content, counts requested information (line count, word count,
     * byte count) and writes to the common output stream
     *
     * @param stream   input stream to read from
     * @param fileName name of file to write to the result. Empty string if reading from the common
     *                 input stream
     *
     * @throws IOException if unable to write to the common output stream
     */
    private void readFromInputStream(InputStream stream, String fileName) throws IOException {
        try (BufferedReader reader = getReader(stream)) {
            while (reader.ready()) {
                String line = reader.readLine();
                lineCount.first++;
                wordsCount.first +=
                    Arrays.stream(line.split(" ")).filter(v -> !v.equals("")).count();
                byteCount.first += line.getBytes(StandardCharsets.UTF_8).length;
            }
            write(lineCount.first, wordsCount.first, byteCount.first, fileName);
            lineCount.second += lineCount.first;
            wordsCount.second += wordsCount.first;
            byteCount.second += byteCount.first;
            lineCount.first = wordsCount.first = byteCount.first = 0L;
        }
    }
    
    /**
     * In cycle counts information from the files
     *
     * @throws WcFileNotFoundException if no file with passed name found
     * @throws IOException             if unable to write to the common output stream
     */
    private void countFromFiles() throws WcFileNotFoundException, IOException {
        for (String fileName : params) {
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                throw new WcFileNotFoundException(
                    "Wc command did not found with  name " + fileName);
            }
            readFromInputStream(FileUtils.getFileAsStream(fileName), fileName);
        }
    }
    
    /**
     * Write to the common output stream. According to the presented flags constructs result
     * string.
     *
     * @param lineCount  current lines count
     * @param wordsCount current words count
     * @param byteCount  current bytes count
     * @param filename   name of file with presented content. Empty if content was from input
     *                   stream
     *
     * @throws IOException if unable to write to the common output stream
     */
    private void write(long lineCount, long wordsCount, long byteCount, String filename)
        throws IOException {
        StringBuilder result = new StringBuilder();
        result.append("\t");
        if (!lineCountOnly && !wordsCountOnly && !byteCountOnly) {
            result.append(lineCount).append("\t\t").append(wordsCount).append("\t\t")
                  .append(byteCount).append("\t\t");
        } else {
            result.append(lineCountOnly ? lineCount + "\t\t" : "")
                  .append(wordsCountOnly ? wordsCount + "\t\t" : "")
                  .append(byteCountOnly ? byteCount + "\t\t" : "");
        }
        Optional<String> file = Optional.ofNullable(filename);
        file.ifPresent(result::append);
        CommandResultSaver.writeToOutput(result + "\n", APPEND_TO_OUTPUT);
    }
    
    @Override
    public boolean printHelp() throws IOException {
        if (help) {
            print(Commands.wc);
            return true;
        }
        return false;
    }
    
    @Override
    public Commands getCommandName() {
        return Commands.wc;
    }
}
