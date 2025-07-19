package me.do31.farmAdder.utils;

import me.do31.farmAdder.FarmAdder;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private static FarmAdder instance;
    private static FileConfiguration config;

    // Cached Settings
    public static boolean WATER_CROP_HARVEST;
    public static boolean DROP_SEEDS;
    public static boolean BONE_MEAL_ENABLED;
    public static boolean PARTICLE_ENABLED;
    public static String PARTICLE_TYPE;
    public static int PARTICLE_AMOUNT;
    public static int PARTICLE_PERIOD;
    public static int PARTICLE_MAX_DISTANCE;

    public static void init(FarmAdder pluginInstance) {
        instance = pluginInstance;
    }

    public static void loadConfig() {
        if (instance == null) {
            System.out.println("[FarmAdder] FATAL ERROR: ConfigManager has not been initialized!");
            return;
        }

        if (!instance.getDataFolder().exists()) {
            instance.getDataFolder().mkdir();
        }

        instance.saveDefaultConfig();
        config = instance.getConfig();
        config.options().copyDefaults(true);
        saveConfig();

        cacheSettings();

        ShopManager.loadShopConfig();
        CropsConfigManager.loadCropsConfigs();
    }

    public static void reloadConfig() {
        instance.reloadConfig();
        config = instance.getConfig();
        cacheSettings();

        ShopManager.loadShopConfig();
        CropsConfigManager.loadCropsConfigs();
    }

    private static void cacheSettings() {
        WATER_CROP_HARVEST = config.getBoolean("물_농작물_수확");
        DROP_SEEDS = config.getBoolean("씨앗_드랍여부");
        BONE_MEAL_ENABLED = config.getBoolean("뼛가루_사용여부");
        PARTICLE_ENABLED = config.getBoolean("파티클_사용여부");
        PARTICLE_TYPE = config.getString("파티클_종류");
        PARTICLE_AMOUNT = config.getInt("파티클_갯수");
        PARTICLE_PERIOD = config.getInt("파티클_주기");
        PARTICLE_MAX_DISTANCE = config.getInt("파티클_최대거리");
    }

    public static String getString(String path) {
        return config.getString(path);
    }

    public static int getInt(String path) {
        return config.getInt(path);
    }

    public static int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public static boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public static void setString(String path, String value) {
        config.set(path, value);
        saveConfig();
        // Update cache immediately
        if (path.equals("파티클_종류")) {
            PARTICLE_TYPE = value;
        }
    }

    public static void setInt(String path, int value) {
        config.set(path, value);
        saveConfig();
        // Update cache immediately
        switch (path) {
            case "파티클_갯수":
                PARTICLE_AMOUNT = value;
                break;
            case "파티클_주기":
                PARTICLE_PERIOD = value;
                break;
            case "파티클_최대거리":
                PARTICLE_MAX_DISTANCE = value;
                break;
        }
    }

    public static void setBoolean(String path, boolean value) {
        config.set(path, value);
        saveConfig();
        // Update cache immediately
        switch (path) {
            case "뼛가루_사용여부":
                BONE_MEAL_ENABLED = value;
                break;
            case "파티클_사용여부":
                PARTICLE_ENABLED = value;
                break;
        }
    }

    public static void saveConfig() {
        try {
            config.save(new File(instance.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}