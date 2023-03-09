package org.itmo.modules;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTests {
    
    Parser parser = new Parser();
    
    @ParameterizedTest
    @MethodSource("testingDifferentStrings")
    public void parsStringTest (String expected, String actual) {
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
    
}
