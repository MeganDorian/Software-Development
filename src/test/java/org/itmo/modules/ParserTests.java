package org.itmo.modules;

import static org.itmo.commands.Commands.cat;
import static org.itmo.commands.Commands.echo;
import static org.itmo.commands.Commands.external;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.itmo.utils.CommandInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ParserTests {
    
    private final Parser parser = new Parser();
    
    static Stream<? extends Arguments> commands() {
        return Stream.of(Arguments.of(List.of("echo sffslk"),
                new CommandInfo(echo, new ArrayList<>(), List.of("sffslk"))),
            Arguments.of(List.of("cat -h smth"),
                new CommandInfo(cat, List.of("-h"), List.of("smth"))),
            Arguments.of(List.of("someCommand"),
                new CommandInfo(external, List.of("someCommand"), new ArrayList<>())),
            Arguments.of(List.of("cat --E some.txt get.txt"),
                new CommandInfo(cat, List.of("--E"), List.of("some.txt", "get.txt"))));
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
            Arguments.of(List.of("this is 'a' string"), "this \"is 'a\"' string"),
            Arguments.of(List.of("if \\\\this \\ is ' \\a  \\\\ | string $"),
                "if \\\\\\this \\\\ is \\'    \\\\\"a  \\\\\" \\| string \\$ "));
    }
    
    static Stream<? extends Arguments> pipes() {
        return Stream.of(Arguments.of("echo ds | cat ds", List.of("echo ds", "cat ds")),
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
                List.of("this is a string", "this is a string")));
    }
    
    static Stream<? extends Arguments> substitutes() {
        return Stream.of(Arguments.of(List.of("echo y"), "echo $x"),
            Arguments.of(List.of("echo ==c"), "echo $a"),
            Arguments.of(List.of("echo \\y"), "echo \\\\$x"),
            Arguments.of(List.of("echo \\y $x"), "echo \\\\$x \\$x"),
            Arguments.of(List.of("echo \\$x"), "echo \\\\\\$x"),
            Arguments.of(List.of("echo $$x"), "echo $$x"),
            Arguments.of(List.of("echo $$y y"), "echo $$$x $x"),
            Arguments.of(List.of("echo y \\"), "echo $x \\\\"),
            Arguments.of(List.of("y==c"), "$x$a"));
    }
    
    static Stream<? extends Arguments> forInitialisingTest() {
        return Stream.of(Arguments.of("x", "some", "some"),
            Arguments.of("x", "\"just   a   \\\\ string\"", "just a \\\\ string"),
            Arguments.of("x", "'some string'", "some string"));
    }
    
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
    
    @ParameterizedTest
    @MethodSource("testingDifferentStrings")
    public void parseStringTest(List<String> expected, String actual) {
        List<String> result = parser.substitutor(actual);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
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
    
    @ParameterizedTest
    @MethodSource("forInitialisingTest")
    public void initialisingTest(String name, String value, String expect) {
        parser.commandParser(parser.substitutor(name + "=" + value));
        List<CommandInfo> commands = parser.commandParser(parser.substitutor("echo $" + name));
        assertEquals(expect, String.join(" ", commands.get(0).getParams()));
    }
    
}
