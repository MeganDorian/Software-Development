package org.itmo.modules;

import static org.itmo.commands.Commands.cat;
import static org.itmo.commands.Commands.echo;
import static org.itmo.commands.Commands.wc;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.itmo.modules.executor.Executor;
import org.itmo.utils.CommandInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ExecutorTests {
    
    private final Executor executor = new Executor();
    
    public static Stream<? extends Arguments> forRunAllCommandsTest() {
        return Stream.of(Arguments.of(
            List.of(new CommandInfo(echo, Collections.emptyList(), List.of("some string"))),
            "some string\n"), Arguments.of(
            List.of(new CommandInfo(cat, Collections.emptyList(), List.of("some text")),
                    new CommandInfo(wc, Collections.emptyList(), Collections.emptyList())),
            "Cat command did " + "not found file with name some text\n"), Arguments.of(
            List.of(new CommandInfo(echo, Collections.emptyList(), List.of("text")),
                    new CommandInfo(echo, Collections.emptyList(), Collections.emptyList())),
            "\n"));
    }
    
    @ParameterizedTest
    @MethodSource("forRunAllCommandsTest")
    public void runAllCommandsTest(List<CommandInfo> allCommands, String expected) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        executor.run(allCommands);
        
        String actual = new String(outputStream.toByteArray()).replaceAll("\r", "");
        
        assertEquals(expected, actual);
    }
    
}
