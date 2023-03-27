package org.itmo.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.itmo.commands.Command;
import org.itmo.commands.cat.Cat;
import org.itmo.commands.echo.Echo;
import org.itmo.commands.wc.Wc;
import org.itmo.modules.executor.Executor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ExecutorTests {
    
    private final Executor executor = new Executor();
    
    public static Stream<? extends Arguments> forRunAllCommandsTest() {
        return Stream.of(Arguments.of(List.of(new Echo(List.of("some string"))), "some string\n"),
                         Arguments.of(List.of(new Cat(false, false, false, List.of("some text")),
                                              new Wc(false, false, false, false,
                                                     Collections.emptyList())),
                                      "Cat command did " + "not found file with name some text\n"),
                         Arguments.of(
                             List.of(new Echo(List.of("text")), new Echo(Collections.emptyList())),
                             "\n"));
    }
    
    @ParameterizedTest
    @MethodSource("forRunAllCommandsTest")
    public void runAllCommandsTest(List<Command> allCommands, String expected) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        executor.run(allCommands);
        
        String actual = new String(outputStream.toByteArray()).replaceAll("\r", "");
        
        assertEquals(expected, actual);
    }
    
}
