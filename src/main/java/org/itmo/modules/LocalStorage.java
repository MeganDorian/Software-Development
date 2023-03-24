package org.itmo.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LocalStorage {
    private final Map<String, String> variables;
    
    /**
     * Initialising a container for storing local variables
     */
    public LocalStorage() {
        variables = new HashMap<>();
    }
    
    /**
     * Searches the list of local variables by the given name.
     *
     * @param variableName - name of the variable whose value will be returned
     *
     * @return Optional which contains variable value or empty Optional if variable nonexistent
     */
    
    public Optional<String> get(String variableName) {
        return Optional.ofNullable(variables.get(variableName));
    }
    
    /**
     * Adds the variable and its value to the global list. If the variable already exists, the value
     * will be replaced.
     *
     * @param variableName  - name of the variable
     * @param variableValue - variable value
     */
    public void set(String variableName, String variableValue) {
        variables.put(variableName, variableValue);
    }
}
