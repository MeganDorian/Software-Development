package org.itmo.utils;

import java.util.List;

public class CommandInfo {
    private final String commandName;
    
    private final List<String> flags;
    
    private final List<String> params;
    
    public CommandInfo(final String commandName,
                       final List<String> flags,
                       final List<String> params) {
        this.commandName = commandName;
        this.flags = flags;
        this.params = params;
    }
    
    public String getCommandName() {
        return commandName;
    }
    
    public List<String> getFlags() {
        return flags;
    }
    
    public List<String> getParams() {
        return params;
    }
    
    public void addFlag(String flag) {
        if (!getFlags().contains(flag)) {
            getFlags().add(flag);
        }
    }
    
    public void addParams(String param) {
        getParams().add(param);
    }
}
