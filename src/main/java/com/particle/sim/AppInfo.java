package com.particle.sim;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppInfo {
    private static final String UNKNOWN_VERSION = "dev";
    private static final Properties PROPERTIES = loadProperties();

    private AppInfo() {
    }

    public static String version() {
        return PROPERTIES.getProperty("app.version", UNKNOWN_VERSION);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = AppInfo.class.getResourceAsStream("/app.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ignored) {
            return properties;
        }
        return properties;
    }
}
