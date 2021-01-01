package com.anywhich.mc.configutil;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class Config {
    private JavaPlugin plugin;

    public Config() {}

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void saveDefaults() {
        load();
        save();
    }

    public void save() {
        saveToConfigUnderNamespace(plugin.getConfig(), "");
        plugin.saveConfig();
    }

    public void load() {
        plugin.reloadConfig();
        loadFromConfigUnderNamespace(plugin.getConfig(), "");
    }

    private void saveToConfigUnderNamespace(FileConfiguration configFile, String namespace) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                String path = getPath(namespace, field);
                Object value = field.get(this);
                if (value instanceof Config) ((Config)value).saveToConfigUnderNamespace(configFile, path);
                else configFile.set(path, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFromConfigUnderNamespace(FileConfiguration configFile, String namespace) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                String path = getPath(namespace, field);
                Object value = configFile.get(path, field.get(this));
                if (field.get(this) instanceof Config) ((Config)field.get(this)).loadFromConfigUnderNamespace(configFile, path);
                else field.set(this, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPath(String namespace, Field field) {
        return namespace.isEmpty() ? field.getName() : namespace + "." + field.getName();
    }
}

