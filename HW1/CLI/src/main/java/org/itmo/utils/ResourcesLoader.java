package org.itmo.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Loads resources files and properties from config file
 */
@UtilityClass
public class ResourcesLoader {
    
    /**
     * Load file from resources
     *
     * @param fileName file name to load
     * @return stream with content of loaded file
     */
    public InputStream getFileFromResource(String fileName) {
        InputStream stream = ResourcesLoader.class.getClassLoader().getResourceAsStream(fileName);
        if (stream == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        return stream;
    }
    
    /**
     * Loads property from config file
     * @param propertyName name of property to load
     * @return loaded property, null if no property found
     */
    public String getProperty(String propertyName) {
        String property = null;
        try (InputStreamReader streamReader =
                     new InputStreamReader(getFileFromResource("config.properties"), StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(streamReader);
            property = properties.getProperty(propertyName);
            if (property == null) {
                throw new IllegalArgumentException("No property " + propertyName + " found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return property;
    }
}
