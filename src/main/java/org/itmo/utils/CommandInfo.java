package org.itmo.utils;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.itmo.commands.Commands;

@Getter
@AllArgsConstructor
public class CommandInfo {
    private final Commands commandName;
    
    private final List<String> flags;
    
    private final List<String> params;
    
    public void addParams(String param) {
        getParams().add(param);
    }
}
