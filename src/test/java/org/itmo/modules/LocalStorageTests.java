package org.itmo.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocalStorageTests {
    
    static Stream<? extends Arguments> argumentsForTestGetVariables() {
        return Stream.of(Arguments.of("x", "val", Optional.of("val")),
                         Arguments.of("y", "another value", Optional.of("another value")));
    }
    
    @Test
    public void getaNonexistentVariableTest() {
        LocalStorage localStorage = new LocalStorage();
        assertEquals(Optional.empty(), localStorage.get("some variable"));
    }
    
    @ParameterizedTest
    @MethodSource("argumentsForTestGetVariables")
    public void setAndGetVariablesTest(String variableName, String variableValue,
                                       Optional<String> answer) {
        LocalStorage localStorage = new LocalStorage();
        localStorage.set(variableName, variableValue);
        assertEquals(answer, localStorage.get(variableName));
    }
}
