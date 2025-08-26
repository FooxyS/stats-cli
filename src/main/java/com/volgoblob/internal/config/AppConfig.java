package com.volgoblob.internal.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try {
            InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties");
            if (in == null) {
                throw new AppConfigException("File application.properties is not found.");
            }
            props.load(in);
        } catch (Exception e) {
            throw new AppConfigException("Somthing went wrong while reading config", e);
        }
    }

    /**
     * return string value that is mapped with the passed key in application config
     * @param key name of the property you want to find
     * @return string value that was found with the passed key
     * @throws AppConfigException if passed key is null
     * @throws AppConfigException if property not found with passed key
     */
    public static String getVariableFromConfig(String key) {
        if (key == null) {
            throw new AppConfigException("Passed key in getVariableFromConfig is null");
        }

        String value = props.getProperty(key);

        if (value == null) {
            throw new AppConfigException("Property value was not found with passed key: " + key);
        }

        return value;
    }
}
