package org.itmo.modules;

import org.itmo.utils.CommandInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTests {
    
    Parser parser = new Parser();
    
    static Stream<? extends Arguments> pipes() {
        return Stream.of(
                Arguments.of("echo ds | cat ds", List.of(new StringBuilder("echo ds"), new StringBuilder("cat ds"))),
                Arguments.of("echo ds|cat ds", List.of(new StringBuilder("echo ds"), new StringBuilder("cat ds"))),
                Arguments.of("echo ds|", List.of(new StringBuilder("echo ds"))),
                Arguments.of("echo ds| ", List.of(new StringBuilder("echo ds"))),
                Arguments.of("echo ds\\| ", List.of(new StringBuilder("echo ds| "))),
                Arguments.of("echo ds \\| cat", List.of(new StringBuilder("echo ds | cat"))),
                Arguments.of("echo ds \\\\| cat", List.of(new StringBuilder("echo ds \\"), new StringBuilder("cat"))),
                Arguments.of("echo 'ds |' cat", List.of(new StringBuilder("echo ds | cat"))),
                Arguments.of("echo \"ds |\" cat", List.of(new StringBuilder("echo ds | cat"))),
                Arguments.of("echo \"ds $|\" cat", List.of(new StringBuilder("echo ds  cat"))),
                Arguments.of("this is \"a\" string | this is 'a' string",
                        List.of(new StringBuilder("this is a string"), new StringBuilder("this is a string")))
        );
    }
    
    static Stream<? extends Arguments> testingDifferentStrings() {
        return Stream.of(
                Arguments.of(List.of(new StringBuilder("this is just (a) string")), "this is just (a) string"),
                Arguments.of(List.of(new StringBuilder("this is some string")), "this is 'some' string"),
                Arguments.of(List.of(new StringBuilder("this is string")), "this is \"string\""),
                Arguments.of(List.of(new StringBuilder("this is 'a' string")), "this \"is 'a'\" string"),
                Arguments.of(List.of(new StringBuilder("this is \"a\" string")), "this 'is \"a\"' string"),
                Arguments.of(List.of(new StringBuilder("this is a string'")), "this is a string'"),
                Arguments.of(List.of(new StringBuilder("this is a string\"")), "this is a string\""),
                Arguments.of(List.of(new StringBuilder("this is \"a\" string")), "this 'is \"a'\" string"),
                Arguments.of(List.of(new StringBuilder("this is 'a' string")), "this \"is 'a\"' string")
        );
    }
    
    @ParameterizedTest
    @MethodSource("testingDifferentStrings")
    public void parseStringTest(List<StringBuilder> expected, String actual) {
        List<StringBuilder> result = parser.substitutor(actual);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), result.get(i).toString());
        }
    }
    
    static Stream<? extends Arguments> substitutes() {
        return Stream.of(
                Arguments.of(List.of(new StringBuilder("echo y")), "echo $x"),
                Arguments.of(List.of(new StringBuilder("echo ==c")), "echo $a"),
                Arguments.of(List.of(new StringBuilder("echo \\y")), "echo \\\\$x"),
                Arguments.of(List.of(new StringBuilder("echo \\y $x")), "echo \\\\$x \\$x"),
                Arguments.of(List.of(new StringBuilder("echo \\$x")), "echo \\\\\\$x"),
                Arguments.of(List.of(new StringBuilder("echo $$x")), "echo $$x"),
                Arguments.of(List.of(new StringBuilder("echo $$y y")), "echo $$$x $x"),
                Arguments.of(List.of(new StringBuilder("echo y \\")), "echo $x \\\\")
        );
    }
    
    @ParameterizedTest
    @MethodSource("substitutes")
    public void shouldCorrectlySubstitute(List<StringBuilder> expected, String substitute) {
        parser.commandParser("x=y");
        parser.commandParser("a===c");
        List<StringBuilder> result = parser.substitutor(substitute);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), result.get(i).toString());
        }
    }
    
    static Stream<? extends Arguments> commands() {
        return Stream.of(
                Arguments.of("echo sffslk", new CommandInfo("echo",
                        new ArrayList<>(),
                        List.of("sffslk"))),
                Arguments.of("cat -h smth", new CommandInfo("cat",
                        List.of("-h"),
                        List.of("smth"))),
                Arguments.of("someCommand", new CommandInfo("someCommand",
                        new ArrayList<>(),
                        new ArrayList<>())),
                Arguments.of("cat --E some.txt get.txt", new CommandInfo("cat",
                        List.of("--E"),
                        List.of("some.txt", "get.txt")))
        );
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
    
    @ParameterizedTest
    @MethodSource("pipes")
    public void shouldPareWithPipes(String input, List<StringBuilder> expected) {
        List<StringBuilder> result = parser.substitutor(input);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString().trim(), result.get(i).toString().trim());
        }
    }
    
}
