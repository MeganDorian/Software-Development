package org.itmo.commands.cat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.exceptions.CatFileNotFoundException;
import org.itmo.modules.Reader;
import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtils;
import org.itmo.utils.ResourcesLoader;

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
        if (!printHelp()) {
            int lineNumber = 1;
            for (String fileName : params) {
                File file = new File(fileName);
                if (!file.exists() || !file.isFile()) {
                    throw new CatFileNotFoundException(
                        "Cat command did not found file with name " + fileName);
                }
                
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(FileUtils.getFileAsStream(fileName),
                        StandardCharsets.UTF_8))) {
                    StringBuilder line = new StringBuilder();
                    String l = reader.readLine();
                    // if no temporary result in pipeResultFile, reads system input stream
                    if (l == null && fileName.equals(CommandResultSaver.getResultPath())) {
                        if (flags.contains(CatFlags.N)) {
                            line.append("\t").append(1).append("\t\t");
                        }
                        line.append(new Reader().readInput().get());
                        if (flags.contains(CatFlags.E)) {
                            line.append("$");
                        }
                        CommandResultSaver.savePipeCommandResult(line.toString());
                        return;
                    }
                    while (l != null) {
                        if (flags.contains(CatFlags.N)) {
                            line.append("\t").append(lineNumber).append("\t\t");
                            lineNumber++;
                        }
                        line.append(l);
                        if (flags.contains(CatFlags.E)) {
                            line.append("$");
                        }
                        CommandResultSaver.savePipeCommandResult(line + "\n");
                        line.delete(0, line.length());
                        l = reader.readLine();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    @Override
    public boolean printHelp() {
        if (!flags.isEmpty() && (flags.contains(CatFlags.HELP) || flags.contains(CatFlags.H))) {
            String helpFileName = ResourcesLoader.getProperty(Commands.cat + ".help");
            CommandResultSaver.saveFullPipeCommandResult(helpFileName);
            return true;
        }
        return false;
    }
}