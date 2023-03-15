package org.itmo.modules;

import org.itmo.utils.CommandInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.itmo.commands.Commands.external;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTests {
    
    private final Parser parser = new Parser();
    
    @ParameterizedTest
    @MethodSource("commands")
    public void parseCommands(List<String> line, CommandInfo expected) {
        CommandInfo commandInfo = parser.commandParser(line).get(0);
        assertEquals(commandInfo.getCommandName(), expected.getCommandName());
        assertEquals(expected.getFlags().size(), commandInfo.getFlags().size());
        assertEquals(expected.getParams().size(), commandInfo.getParams().size());
        for (int i = 0; i < expected.getFlags().size(); i++) {
            assertEquals(commandInfo.getFlags().get(i), expected.getFlags().get(i));
        }
        for (int i = 0; i < expected.getParams().size(); i++) {
            assertEquals(commandInfo.getParams().get(i), expected.getParams().get(i));
        }
    }
    
    static Stream<? extends Arguments> commands() {
        return Stream.of(
//                Arguments.of(List.of("echo sffslk"), new CommandInfo(echo,
//                        new ArrayList<>(),
//                        List.of("sffslk"))),
//                Arguments.of(List.of("cat -h smth"), new CommandInfo(cat,
//                        List.of("-h"),
//                        List.of("smth"))),
                Arguments.of(List.of("someCommand"), new CommandInfo(external,
                        List.of("someCommand"),
                        new ArrayList<>()))
//                Arguments.of(List.of("cat --E some.txt get.txt"), new CommandInfo(cat,
//                        List.of("--E"),
//                        List.of("some.txt", "get.txt")))
        );
    }
    
    @ParameterizedTest
    @MethodSource("testingDifferentStrings")
    public void parseStringTest(List<String> expected, String actual) {
        List<String> result = parser.substitutor(actual);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }
    
    static Stream<? extends Arguments> testingDifferentStrings() {
        return Stream.of(
                Arguments.of(List.of("this is just (a) string"), "this is just (a) string"),
                Arguments.of(List.of("this is some string"), "this is 'some' string"),
                Arguments.of(List.of("this is string"), "this is \"string\""),
                Arguments.of(List.of("this is 'a' string"), "this \"is 'a'\" string"),
                Arguments.of(List.of("this is \"a\" string"), "this 'is \"a\"' string"),
                Arguments.of(List.of("this is a string'"), "this is a string'"),
                Arguments.of(List.of("this is a string\""), "this is a string\""),
                Arguments.of(List.of("this is \"a\" string"), "this 'is \"a'\" string"),
                Arguments.of(List.of("this is 'a' string"), "this \"is 'a\"' string")
        );
    }
    
    @ParameterizedTest
    @MethodSource("pipes")
    public void shouldParseWithPipes(String input, List<String> expected) {
        List<String> result = parser.substitutor(input);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).trim(), result.get(i).trim());
        }
    }
    
    static Stream<? extends Arguments> pipes() {
        return Stream.of(
                Arguments.of("echo ds | cat ds", List.of("echo ds", "cat ds")),
                Arguments.of("echo ds|cat ds", List.of("echo ds", "cat ds")),
                Arguments.of("echo ds|", List.of("echo ds")),
                Arguments.of("echo ds| ", List.of("echo ds")),
                Arguments.of("echo ds\\| ", List.of("echo ds| ")),
                Arguments.of("echo ds \\| cat", List.of("echo ds | cat")),
                Arguments.of("echo ds \\\\| cat", List.of("echo ds \\", "cat")),
                Arguments.of("echo 'ds |' cat", List.of("echo ds | cat")),
                Arguments.of("echo \"ds |\" cat", List.of("echo ds | cat")),
                Arguments.of("echo \"ds $|\" cat", List.of("echo ds  cat")),
                Arguments.of("this is \"a\" string | this is 'a' string",
                        List.of("this is a string", "this is a string"))
        );
    }
    
    @ParameterizedTest
    @MethodSource("substitutes")
    public void shouldCorrectlySubstitute(List<String> expected, String substitute) {
        parser.commandParser(List.of("x=y"));
        parser.commandParser(List.of("a===c"));
        List<String> result = parser.substitutor(substitute);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }
    
    static Stream<? extends Arguments> substitutes() {
        return Stream.of(
                Arguments.of(List.of("echo y"), "echo $x"),
                Arguments.of(List.of("echo ==c"), "echo $a"),
                Arguments.of(List.of("echo \\y"), "echo \\\\$x"),
                Arguments.of(List.of("echo \\y $x"), "echo \\\\$x \\$x"),
                Arguments.of(List.of("echo \\$x"), "echo \\\\\\$x"),
                Arguments.of(List.of("echo $$x"), "echo $$x"),
                Arguments.of(List.of("echo $$y y"), "echo $$$x $x"),
                Arguments.of(List.of("echo y \\"), "echo $x \\\\")
        );
    }
    
}
