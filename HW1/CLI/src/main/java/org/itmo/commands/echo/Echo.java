package org.itmo.commands.echo;

import org.itmo.commands.Command;
import org.itmo.utils.CommandInfo;

import java.util.List;

public class Echo implements Command {
    private final List<String> output;
    
    public Echo(CommandInfo commandInfo) {
        output = commandInfo.getParams();
    }
    
    @Override
    public void execute() {
        output.forEach(s -> System.out.print(s + " "));
    }
}
