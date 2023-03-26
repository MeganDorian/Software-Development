package org.itmo.modules;

import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileUtilsForTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.itmo.commands.Commands.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecutorTests {
    
    private final Executor executor = new Executor();
    
    @ParameterizedTest
    @MethodSource("forRunAllCommandsTest")
    public void runAllCommandsTest(List<CommandInfo> allCommands, String expected) {
        executor.run(allCommands);
        String actual = FileUtilsForTest.loadFullContent(CommandResultSaver.getResult().toFile());
        assertEquals(expected, actual);
    }
    
    public static Stream<? extends Arguments> forRunAllCommandsTest() {
        return Stream.of(
                Arguments.of(
                        List.of(new CommandInfo(echo, Collections.emptyList(), List.of("some string"))),
                        "some string"),
                Arguments.of(
                        List.of(new CommandInfo(cat, Collections.emptyList(), List.of("some text")),
                                new CommandInfo(wc, Collections.emptyList(), Collections.emptyList())),
                        ""),
                Arguments.of(List.of(new CommandInfo(echo, Collections.emptyList(), List.of("text")),
                                new CommandInfo(echo, Collections.emptyList(), Collections.emptyList())),
                        "\n")
        );
    }
    
}
