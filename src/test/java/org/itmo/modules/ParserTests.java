package org.itmo.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.itmo.commands.Command;
import org.itmo.commands.Commands;
import org.itmo.commands.cat.Cat;
import org.itmo.commands.echo.Echo;
import org.itmo.commands.external.External;
import org.itmo.commands.grep.Grep;
import org.itmo.modules.parser.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ParserTests {
    
    private final Parser parser = new Parser();
    
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
                         Arguments.of("x", "\"just   a   \\\\ string\"", "just   a   \\\\ string"),
                         Arguments.of("x", "'some string'", "some string"));
    }
    
    
    private void checkCat(Cat expected, Cat actual) {
        assertEquals(expected.getCommandName(), actual.getCommandName());
        assertEquals(expected.isDisplayDollar(), actual.isDisplayDollar());
        assertEquals(expected.isHelp(), actual.isHelp());
        assertEquals(expected.isNumberOfLine(), actual.isNumberOfLine());
        assertEquals(expected.getFiles(), actual.getFiles());
    }
    
    @Test
    public void shouldParseCat() {
        List<String> toParse = List.of("cat -h   --E -n file1   file2", "cat --N");
        Cat cat = (Cat) parser.commandParser(toParse).get(0);
        checkCat(new Cat(true, true, true, List.of("file1", "file2")), cat);
    }
    
    @Test
    public void shouldParseEcho() {
        List<String> toParse = List.of("echo something");
        Echo echo = (Echo) parser.commandParser(toParse).get(0);
        assertEquals(Commands.echo, echo.getCommandName());
        assertEquals(List.of("something"), echo.getParamsToPrint());
    }
    
    @Test
    public void shouldParseExternal() {
        List<String> toParse = List.of("someCommand with parameters");
        External external = (External) parser.commandParser(toParse).get(0);
        assertEquals(Commands.external, external.getCommandName());
        assertEquals(List.of("someCommand", "with", "parameters"), external.getParams());
    }
    
    private void checkGrep(Grep expected, Grep actual) {
        assertEquals(expected.getCommandName(), actual.getCommandName());
        assertEquals(expected.isSearchFullWord(), actual.isSearchFullWord());
        assertEquals(expected.isCaseInsensitive(), actual.isCaseInsensitive());
        assertEquals(expected.getLineCountToPrint(), actual.getLineCountToPrint());
        assertEquals(expected.getPatternAndFiles(), actual.getPatternAndFiles());
    }
    
    @Test
    public void shouldParseGrep() {
        List<String> toParse =
            List.of("grep -i pattern file1 file2", "grep -w pattern -A 10 " + "file1");
        List<Command> commands = parser.commandParser(toParse);
        checkGrep(new Grep(false, true, 0, List.of("pattern", "file1", "file2")),
                  (Grep) commands.get(0));
        checkGrep(new Grep(true, false, 10, List.of("pattern", "file1")), (Grep) commands.get(1));
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
        Echo echo = (Echo) parser.commandParser(parser.substitutor("echo $" + name)).get(0);
        assertEquals(expect, String.join(" ", echo.getParamsToPrint()));
    }
    
}
