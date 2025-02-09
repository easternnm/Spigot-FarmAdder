package me.do31.farmAdder.utils;

import me.do31.farmAdder.FarmAdder;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private static final FarmAdder instance = FarmAdder.getInstance();
    private static FileConfiguration config;

    public static void loadConfig() {

        if(!instance.getDataFolder().exists()) {
            instance.getDataFolder().mkdir();
        }

        instance.saveDefaultConfig();

        config = instance.getConfig();
        config.options().copyDefaults(true);
        saveConfig();
    }

    public static void reloadConfig() {
        instance.reloadConfig();
        config = instance.getConfig();
    }

    public static String getString(String path) {
        return config.getString(path);
    }

    public static int getInt(String path) {
        return config.getInt(path);
    }

    public static boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public static void setString(String path, String value) {
        config.set(path, value);
        saveConfig();
    }

    public static void setInt(String path, int value) {
        config.set(path, value);
        saveConfig();
    }

    public static void setBoolean(String path, boolean value) {
        config.set(path, value);
        saveConfig();
    }

    public static void saveConfig() {
        instance.saveConfig();
    }
}