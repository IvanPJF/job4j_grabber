package ru.job4j.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Logger LOG = LogManager.getLogger(Config.class.getName());

    private final Properties values = new Properties();

    public Config(String properties) {
        this.init(properties);
    }

    public void init(String properties) {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(properties)) {
            this.values.load(in);
            LOG.trace("The properties file is a [{}]", properties);
        } catch (Exception e) {
            LOG.error(String.format("Error reading from file [%s]; %s", properties, e.getMessage()), e);
        }
    }

    public String get(String key) {
        return this.values.getProperty(key);
    }
}

