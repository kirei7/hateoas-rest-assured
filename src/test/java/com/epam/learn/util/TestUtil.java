package com.epam.learn.util;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.Properties;

@Slf4j
public final class TestUtil {

    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            URL url = Resources.getResource("test.properties");
            File file = new File(url.toURI());
            properties.load(Files.asByteSource(file).openStream());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    public static String getProperty(String name) {
        return properties.getProperty(name);
    }
}
