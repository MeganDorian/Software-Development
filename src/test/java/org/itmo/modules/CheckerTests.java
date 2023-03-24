package org.itmo.modules;

import static org.itmo.commands.Commands.cat;
import static org.itmo.commands.Commands.echo;
import static org.itmo.commands.Commands.exit;
import static org.itmo.commands.Commands.external;
import static org.itmo.commands.Commands.pwd;
import static org.itmo.commands.Commands.wc;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.itmo.exceptions.FlagNotFoundException;
import org.itmo.utils.CommandInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CheckerTests {
    
    Checker checker = new Checker();
    
    public static Stream<? extends Arguments> forCheckForNotExistsFlagsAllCommands() {
        return Stream.of(
            Arguments.of(List.of(new CommandInfo(cat, List.of("-er"), new ArrayList<>()))),
            Arguments.of(List.of(new CommandInfo(pwd, List.of("-er"), new ArrayList<>()))),
            Arguments.of(List.of(new CommandInfo(wc, List.of("-er"), new ArrayList<>()))));
    }
    
    public static Stream<? extends Arguments> forCheckInternalCommandsWithoutFlags() {
        return Stream.of(Arguments.of(
            List.of(new CommandInfo(echo, new ArrayList<>(), new ArrayList<>()),
                new CommandInfo(exit, new ArrayList<>(), new ArrayList<>()))));
    }
    
    public static Stream<? extends Arguments> forCheckCommandIsInternal() {
        return Stream.of(Arguments.of("cat"), Arguments.of("echo"), Arguments.of("exit"),
            Arguments.of("pwd"), Arguments.of("wc"));
    }
    
    public static Stream<? extends Arguments> forCheckCommandIsExternal() {
        return Stream.of(Arguments.of("Cat"), Arguments.of("someCommand"), Arguments.of("Exit"));
    }
    
    @Test
    public void externalCommandTest() {
        assertDoesNotThrow(() -> checker.checkCommand(
            List.of(new CommandInfo(external, List.of("someCommand"), new ArrayList<>()))));
    }
    
    @ParameterizedTest
    @MethodSource("forCheckForNotExistsFlagsAllCommands")
    public void checkForNotExistsFlagsAllCommands(List<CommandInfo> commands) {
        assertThrows(FlagNotFoundException.class, () -> checker.checkCommand(commands));
    }
    
    @ParameterizedTest
    @MethodSource("forCheckInternalCommandsWithoutFlags")
    public void checkInternalCommandsWithoutFlags(List<CommandInfo> commands) {
        assertDoesNotThrow(() -> checker.checkCommand(commands));
    }
    
    @ParameterizedTest
    @MethodSource("forCheckCommandIsInternal")
    public void checkCommandIsInternal(String nameCommand) {
        assertTrue(Checker.checkCommandIsInternal(nameCommand));
    }
    
    @ParameterizedTest
    @MethodSource("forCheckCommandIsExternal")
    public void checkCommandIsExternal(String nameCommand) {
        assertFalse(Checker.checkCommandIsInternal(nameCommand));
    }
    
}
