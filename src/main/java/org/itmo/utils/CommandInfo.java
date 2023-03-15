package org.itmo.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommandInfo {
    private final String commandName;
    
    private final List<String> flags;
    
    private final List<String> params;
    
    public void addParams(String param) {
        getParams().add(param);
    }
}
