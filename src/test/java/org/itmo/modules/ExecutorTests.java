package org.itmo.modules;

import org.itmo.utils.CommandInfo;
import org.itmo.utils.CommandResultSaver;
import org.itmo.utils.FileInfo;
import org.itmo.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecutorTests {
    
    Executor executor = new Executor();
    
    @ParameterizedTest
    @MethodSource("forRunAllCommandsTest")
    public void runAllCommandsTest(List<CommandInfo> allCommands, String result) {
        executor.run(allCommands);
        FileInfo fInfo = FileUtils.getFileInfo(CommandResultSaver.getResult().toFile().getPath(), false);
        Optional<String> line = FileUtils.loadLineFromFile(fInfo);
        StringBuilder actual = new StringBuilder();
        while (line.isPresent()) {
            actual.append(line.get());
            line = FileUtils.loadLineFromFile(fInfo);
        }
        assertEquals(result, actual.toString());
    }
    
    public static Stream<? extends Arguments> forRunAllCommandsTest() {
        return Stream.of(Arguments.of(
                List.of(new CommandInfo("echo", new ArrayList<>(), List.of("some string"))), "some string"),
                         Arguments.of(List.of(new CommandInfo("cat", new ArrayList<>(), List.of("some text")),
                                              new CommandInfo("wc", new ArrayList<>(), new ArrayList<>())), ""));
    }
    
}
