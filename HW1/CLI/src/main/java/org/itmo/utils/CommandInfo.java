package org.itmo.utils;

import lombok.Getter;

import java.util.List;

@Getter
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
    
    public void addFlag(String flag) {
        if (!getFlags().contains(flag)) {
            getFlags().add(flag);
        }
    }
    
    public void addParams(String param) {
        getParams().add(param);
    }
}
