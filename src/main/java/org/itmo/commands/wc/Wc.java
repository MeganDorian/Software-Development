package org.itmo.commands.wc;

import static org.itmo.utils.command.CommandResultSaverFlags.APPEND_TO_OUTPUT;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.WcFileNotFoundException;
import org.itmo.modules.Reader;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.FileUtils;
import org.itmo.utils.Pair;
import org.itmo.utils.command.CommandResultSaver;

/**
 * WC command to count
 */
public class Wc implements Command {
    private final List<WcFlags> flags;
    private final List<String> params;
    
    /**
     * Count of lines <br> first - count in for the current parameter <br> second - count in total
     */
    private final Pair<Long> lineCount;
    
    /**
     * Count of word <br> first - count in for the current parameter <br> second - count in total
     */
    private final Pair<Long> wordsCount;
    
    /**
     * Count of bytes <br> first - count in for the current parameter <br> second - count in total
     */
    private final Pair<Long> byteCount;
    
    public Wc(CommandInfo commandInfo) {
        flags = new ArrayList<>();
        commandInfo.getFlags().forEach(
            flag -> flags.add(WcFlags.valueOf(flag.replaceAll("^-{1,2}", "").toUpperCase())));
        params = commandInfo.getParams();
        lineCount = new Pair<>(0L, 0L);
        wordsCount = new Pair<>(0L, 0L);
        byteCount = new Pair<>(0L, 0L);
    }
    
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
    
    private void write(long lineCount, long wordsCount, long byteCount, String filename)
        throws IOException {
        StringBuilder result = new StringBuilder();
        result.append("\t");
        if (flags.size() == 1) {
            if (flags.contains(WcFlags.L)) {
                result.append(lineCount);
            } else if (flags.contains(WcFlags.W)) {
                result.append(wordsCount);
            } else if (flags.contains(WcFlags.C)) {
                result.append(byteCount);
            }
        } else if (flags.size() == 2) {
            result.append(flags.contains(WcFlags.L) ? lineCount + "\t\t" : "")
                  .append(flags.contains(WcFlags.W) ? wordsCount + "\t\t" : "")
                  .append(flags.contains(WcFlags.C) ? byteCount : "");
        } else {
            result.append(lineCount).append("\t\t").append(wordsCount).append("\t\t")
                  .append(byteCount);
        }
        Optional<String> file = Optional.ofNullable(filename);
        file.ifPresent(v -> result.append("\t\t").append(v));
        CommandResultSaver.writeToOutput(result + "\n", APPEND_TO_OUTPUT);
    }
    
    @Override
    public boolean printHelp() throws IOException {
        if (!flags.isEmpty() && (flags.contains(WcFlags.HELP) || flags.contains(WcFlags.H))) {
            print(Commands.wc);
            return true;
        }
        return false;
    }
}
