package org.itmo.commands.wc;

import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.WcFileNotFoundException;
import org.itmo.modules.Reader;
import org.itmo.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * WC command to count
 */
public class Wc implements Command {
    private final List<WcFlags> flags;
    private final List<String> params;
    
    /**
     * Count of lines <br>
     * first - count in for the current parameter <br>
     * second - count in total
     */
    private final Pair<Long> lineCount;
    
    /**
     * Count of word <br>
     * first - count in for the current parameter <br>
     * second - count in total
     */
    private final Pair<Long> wordsCount;
    
    /**
     * Count of bytes <br>
     * first - count in for the current parameter <br>
     * second - count in total
     */
    private final Pair<Long> byteCount;
    
    public Wc(CommandInfo commandInfo) {
        flags = new ArrayList<>();
        commandInfo.getFlags().forEach(flag -> flags.add(WcFlags.valueOf(flag.replaceAll("^-{1,2}", "").toUpperCase())));
        params = commandInfo.getParams();
        lineCount = new Pair<>(0L, 0L);
        wordsCount = new Pair<>(0L, 0L);
        byteCount = new Pair<>(0L, 0L);
    }
    
    @Override
    public void execute() throws WcFileNotFoundException {
        if (!printHelp()) {
            StringBuilder result = new StringBuilder();
            boolean needTotalInfo = params.size() > 1;
            for (String fileName : params) {
                File file = new File(fileName);
                if (!file.exists() || !file.isFile()) {
                    throw new WcFileNotFoundException("Wc command did not found with  name " + fileName);
                }
                
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(FileUtils.getFileAsStream(fileName), StandardCharsets.UTF_8))) {
                    String line = reader.readLine();
                    // if no temporary result in pipeResultFile, reads system input stream
                    if (line == null && fileName.equals(CommandResultSaver.getResultPath())) {
                        line = new Reader().readInput();
                        wordsCount.first = Arrays.stream(line.split(" ")).filter(v -> !v.equals("")).count();
                        byteCount.first = (long) line.getBytes(StandardCharsets.UTF_8).length;
                        write(1, wordsCount.first, byteCount.first, null, result);
                        return;
                    }
                    
                    while (line != null) {
                        lineCount.first++;
                        wordsCount.first += Arrays.stream(line.split(" ")).filter(v -> !v.equals("")).count();
                        byteCount.first += line.getBytes(StandardCharsets.UTF_8).length;
                        line = reader.readLine();
                    }
                    write(lineCount.first, wordsCount.first, byteCount.first, fileName, result);
                    result.delete(0, result.length());
                    lineCount.second += lineCount.first;
                    wordsCount.second += wordsCount.first;
                    byteCount.second += byteCount.first;
                    lineCount.first = wordsCount.first = byteCount.first = 0L;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (needTotalInfo) {
                write(lineCount.second, wordsCount.second, byteCount.second, "total", result);
            }
        }
    }
    
    private void write(long lineCount, long wordsCount, long byteCount,
                       String filename, StringBuilder result) {
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
        CommandResultSaver.savePipeCommandResult(result.toString());
    }
    
    @Override
    public boolean printHelp() {
        if (!flags.isEmpty() && (flags.contains(WcFlags.HELP) || flags.contains(WcFlags.H))) {
            String helpFileName = ResourcesLoader.getProperty(Commands.wc + ".help");
            CommandResultSaver.saveFullPipeCommandResult(helpFileName);
            return true;
        }
        return false;
    }
}
