package org.itmo.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import lombok.experimental.UtilityClass;

/**
 * Loads resources files and properties from config file
 */
@UtilityClass
public class ResourcesLoader {
    
    /**
     * Loads property from config file
     *
     * @param propertyName name of property to load
     *
     * @return loaded property, null if no property found
     */
    public String getProperty(String propertyName) {
        String property = null;
        try (InputStreamReader streamReader = new InputStreamReader(
            FileUtils.getFileFromResource("config.properties"), StandardCharsets.UTF_8)) {
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
