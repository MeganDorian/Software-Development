package org.itmo.modules;

import org.itmo.utils.CommandInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTests {
    
    Parser parser = new Parser();
    
    @ParameterizedTest
    @MethodSource("testingDifferentStrings")
    public void parseStringTest (String expected, String actual) {
        assertEquals(expected, parser.substitutor(actual).toString());
    }
    
    static Stream<? extends Arguments> testingDifferentStrings() {
        return Stream.of(
                Arguments.of("this is just (a) string", "this is just (a) string"),
                Arguments.of("this is some string", "this is 'some' string"),
                Arguments.of("this is string", "this is \"string\""),
                Arguments.of("this is 'a' string", "this \"is 'a'\" string"),
                Arguments.of("this is \"a\" string", "this 'is \"a\"' string"),
                Arguments.of("this is a string'", "this is a string'"),
                Arguments.of("this is a string\"", "this is a string\""),
                Arguments.of("this is \"a\" string", "this 'is \"a'\" string"),
                Arguments.of("this is 'a' string", "this \"is 'a\"' string")
        );
    }
    
    static Stream<? extends Arguments> substitutes() {
        return Stream.of(
                Arguments.of("echo y", "echo $x"),
                Arguments.of("echo ==c", "echo $a"),
                Arguments.of("echo \\y", "echo \\\\$x"),
                Arguments.of("echo \\y $x", "echo \\\\$x \\$x"),
                Arguments.of("echo \\$x", "echo \\\\\\$x"),
                Arguments.of("echo $$x", "echo $$x"),
                Arguments.of("echo $$y y", "echo $$$x $x")
        );
    }
    
    @ParameterizedTest
    @MethodSource("substitutes")
    public void shouldCorrectlySubstitute(String expected, String substitute) {
        parser.commandParser("x=y");
        parser.commandParser("a===c");
        assertEquals(expected, parser.substitutor(substitute).toString());
    }
    
    @ParameterizedTest
    @MethodSource("commands")
    public void parseCommands(String line, CommandInfo expected) {

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
                Arguments.of("echo sffslk", new CommandInfo("echo",
                                                            new ArrayList<>(),
                                                            new ArrayList<>(Arrays.asList("sffslk")))),
                Arguments.of("cat -h smth", new CommandInfo("cat",
                                                            new ArrayList<>(Arrays.asList("-h")),
                                                            new ArrayList<>(Arrays.asList("smth")))),
                Arguments.of("someCommand", new CommandInfo("someCommand",
                                                            new ArrayList<>(),
                                                            new ArrayList<>())),
                Arguments.of("cat --E some.txt get.txt", new CommandInfo("cat",
                                                                         new ArrayList<>(Arrays.asList("--E")),
                                                                         new ArrayList<>(Arrays.asList("some.txt", "get.txt"))))
        );
    }
    
}
