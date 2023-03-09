package com.graemsheppard.chessbot;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class AppConfig {

    public enum Environment {
        DEVELOPMENT,
        PRODUCTION
    }

    private static AppConfig instance;

    @Getter
    private final Environment environment;

    private final HashMap<String, Object> config;

    private AppConfig() {
        environment = System.getenv("ENVIRONMENT").equals("development") ? Environment.DEVELOPMENT : Environment.PRODUCTION;
        config = getConfig();
    }

    public static AppConfig getInstance() {
        if (instance == null)
            instance = new AppConfig();
        return instance;
    }

    private HashMap<String, Object> getConfig() {
        InputStream inputStream;
        if (environment == Environment.DEVELOPMENT) {
            inputStream = getClass().getClassLoader().getResourceAsStream("application.yml");
        } else {
            try {
                inputStream = new FileInputStream("/config/application.yml");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        Yaml yaml = new Yaml();
        return yaml.load(inputStream);
    }

    public Object getValue(String path) {
        HashMap<String, Object> base = config;
        String[] keys = path.split("\\.");
        HashMap<String, Object> current = base;
        for (int i = 0; i < keys.length - 1; i++) {
            current = (HashMap<String, Object>) current.get(keys[i]);
        }
        return current.get(keys[keys.length - 1]);
    }

    public String getString(String path) {
        return (String) getValue(path);
    }
}
