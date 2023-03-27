package org.itmo.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.itmo.commands.grep.Grep;
import org.itmo.exceptions.GrepException;
import org.itmo.utils.command.CommandResultSaver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GrepTests {
    
    @BeforeEach
    public void setUp() {
        CommandResultSaver.initStreams();
    }
    
    static Stream<? extends Arguments> grepContent() {
        return Stream.of(Arguments.of(
            "calling nor her. Within coming figure small things are. Pretended" +
            "name!concluded did repulsive education smallness yet yet described. notname" +
            "Had country man his pressed shewing. No gate dare rose he." +
            "Eyes year if miss he as upon.",
            new Grep(false, false, 3, new ArrayList<>(List.of("small")))), Arguments.of(
            "In as NAME to here them deny wise this." + " As rapid woody my he me which.name!",
            new Grep(true, true, 0, new ArrayList<>(List.of("NAME")))));
    }
    
    private void checkResult(final String expected, final Grep grep) {
        assertDoesNotThrow(grep::execute);
        String actual =
            new String(CommandResultSaver.getOutputStream().toByteArray()).replaceAll("\r", "")
                                                                          .replaceAll("\n", "");
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldReadFromInputStream() {
        Grep grep = new Grep(false, false, 0, new ArrayList<>(List.of("hello$")));
        String expected = "test hello";
        InputStream forTests = new ByteArrayInputStream((expected + "\n").getBytes());
        System.setIn(forTests);
        checkResult(expected, grep);
    }
    
    @ParameterizedTest
    @MethodSource("grepContent")
    public void shouldSearchInFiles(String expected, Grep grep) throws URISyntaxException {
        File file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("grep/grep"))
                   .toURI());
        grep.getPatternAndFiles().add(file.getAbsolutePath());
        
        checkResult(expected, grep);
    }
    
    @Test
    public void shouldThrowFileNotFoundException() {
        Grep grep = new Grep(true, true, 0, new ArrayList<>(List.of("hello", "file")));
        assertThrows(GrepException.class, grep::execute);
    }
    
    @AfterEach
    public void cleanUp() throws IOException {
        CommandResultSaver.closeStreams();
    }
}
