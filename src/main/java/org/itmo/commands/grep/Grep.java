package org.itmo.commands.grep;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.util.List;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;

/**
 * GREP command to search by regex in text
 */
@Parameters(commandDescription = "GREP command to search by regex in text")
public class Grep implements Command {
    @Parameter(names = "-w", description = "searches the whole word to match")
    private boolean searchFullWord;
    
    @Parameter(names = "-i", description = "case-insensitive search")
    private boolean isCaseInsensitive;
    
    @Parameter(names = "-A", description = "how many lines after the match need to print")
    private Integer lineCountToPrint;
    
    @Parameter(description = "The list of files to search in")
    private List<String> files;
    
    @Override
    public void execute() throws Exception {
    
    }
    
    @Override
    public boolean printHelp() throws IOException {
        return false;
    }
    
    @Override
    public Commands getCommandName() {
        return Commands.grep;
    }
}
