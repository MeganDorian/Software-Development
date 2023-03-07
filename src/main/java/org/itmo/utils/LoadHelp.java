package org.itmo.utils;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class LoadHelp {
    public void printHelp(String commandName) {
        printHelpFromInputStream(ResourcesLoader.getFileFromResource(ResourcesLoader.getProperty(commandName + ".help")));
    }
    
    private void printHelpFromInputStream(InputStream stream) {
        try (InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
