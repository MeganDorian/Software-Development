package org.itmo.commands.wc;

import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.WcFileNotFoundException;
import org.itmo.modules.Reader;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.ResourcesLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    
    public Wc(CommandInfo commandInfo) {
        flags = new ArrayList<>();
        commandInfo.getFlags().forEach(flag -> flags.add(WcFlags.valueOf(flag.replaceAll("^-{1,2}", "").toUpperCase())));
        params = commandInfo.getParams();
    }
    
    @Override
    public void execute() throws WcFileNotFoundException {
        if (!printHelp()) {
            StringBuilder result = new StringBuilder();
            long lineCount = 0;
            long wordsCount = 0;
            long byteCount = 0;
            long lineCountTotal = 0;
            long wordsCountTotal = 0;
            long byteCountTotal = 0;
            boolean l = flags.contains(WcFlags.L);
            boolean w = flags.contains(WcFlags.W);
            boolean b = flags.contains(WcFlags.C);
            boolean needTotalInfo = params.size() > 1;
            if (params.isEmpty()) {
                String line = new Reader().readInput();
                wordsCount = Arrays.stream(line.split(" ")).filter(v -> !v.equals("")).count();
                byteCount = line.getBytes(StandardCharsets.UTF_8).length;
                write(l, w, b, 1, wordsCount, byteCount, null, result);
            } else {
                for (String fileName : params) {
                    File file = new File(fileName);
                    if (!file.exists() || !file.isFile()) {
                        throw new WcFileNotFoundException("Wc command did not found with  name " + fileName);
                    }
                    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                        String line = reader.readLine();
                        while (line != null) {
                            lineCount++;
                            wordsCount += Arrays.stream(line.split(" ")).filter(v -> !v.equals("")).count();
                            byteCount += line.getBytes(StandardCharsets.UTF_8).length;
                            line = reader.readLine();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    write(l, w, b, lineCount, wordsCount, byteCount, fileName, result);
                    result.delete(0, result.length());
                    lineCountTotal += lineCount;
                    wordsCountTotal += wordsCount;
                    byteCountTotal += byteCount;
                    lineCount = wordsCount = byteCount = 0;
                }
                
                if (needTotalInfo) {
                    write(l, w, b, lineCountTotal, wordsCountTotal, byteCountTotal, "total", result);
                }
            }
        }
    }
    
    private void write(boolean l, boolean w, boolean b,
                       long lineCount, long wordsCount, long byteCount,
                       String filename, StringBuilder result) {
        result.append("\t");
        if (flags.size() == 1) {
            if (l) {
                result.append(lineCount);
            } else if (w) {
                result.append(wordsCount);
            } else if (b) {
                result.append(byteCount);
            }
        } else if (flags.size() == 2) {
            result.append(l ? lineCount + "\t\t" : "")
                    .append(w ? wordsCount + "\t\t" : "")
                    .append(b ? byteCount : "");
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
