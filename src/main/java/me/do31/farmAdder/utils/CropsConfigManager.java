package me.do31.farmAdder.utils;

import me.do31.farmAdder.FarmAdder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CropsConfigManager {
    private static final FarmAdder instance = FarmAdder.getInstance();
    private static final File cropsFolder = new File(instance.getDataFolder(), "crops");
    private static final Map<String, FileConfiguration> cropsConfigs = new HashMap<>();

    public static void loadCropsConfigs() {
        if (!cropsFolder.exists()) {
            cropsFolder.mkdir();
        }

        File riceFile = new File(cropsFolder, "rice.yml");
        if (!riceFile.exists()) {
            instance.saveResource("rice.yml", false);
            File savedFile = new File(instance.getDataFolder(), "rice.yml");

            if (savedFile.exists()) {
                savedFile.renameTo(riceFile);
            }
        }

        cropsConfigs.clear();
        File[] cropsFiles = cropsFolder.listFiles();

        if (cropsFiles != null) {
            for (File cropFile : cropsFiles) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(cropFile);
                cropsConfigs.put(cropFile.getName().replace(".yml", ""), config);
            }
        }

        instance.getLogger().info("총 " + cropsConfigs.size() + "개의 작물 설정 파일을 로드했습니다.");
    }

    public static String[] getCrops() {
        return cropsConfigs.keySet().toArray(new String[0]);
    }

    public static ItemStack createSeed(String cropName) {
        FileConfiguration config = cropsConfigs.get(cropName);
        if (config == null) {
            return null;
        }

        Material material = Material.valueOf(config.getString("씨앗.id"));
        ItemStack seedItem = new ItemStack(material);
        ItemMeta meta = seedItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("씨앗.name")));
            if (config.contains("씨앗.custom_model_data")) {
                meta.setCustomModelData(config.getInt("씨앗.custom_model_data"));
            }

            List<String> lore = config.getStringList("씨앗.lore");
            if (lore != null) {
                List<String> translatedLore = new ArrayList<>();
                for (String line : lore) {
                    translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(translatedLore);
            }

            if (config.contains("crop_data")) {
                NamespacedKey tags = new NamespacedKey(instance, "crop_data");
                String cropData = config.getString("crop_data");
                meta.getPersistentDataContainer().set(tags, PersistentDataType.STRING, cropData);
            }

            seedItem.setItemMeta(meta);
        }

        return seedItem;
    }

    public static List<ItemStack> createAwards(String cropName, int fortuneLevel) {
        FileConfiguration config = cropsConfigs.get(cropName);
        if (config == null) {
            return null;
        }

        List<ItemStack> awards = new ArrayList<>();
        ConfigurationSection rewardsSection = config.getConfigurationSection("보상");
        if(rewardsSection != null) {
            for(String key : rewardsSection.getKeys(false)) {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(key);
                if(rewardSection != null) {
                    Material material = Material.valueOf(rewardSection.getString("id"));
                    int amount = rewardSection.getInt("amount", 0);
                    boolean isFortune = rewardSection.getBoolean("fortune", false);
                    String type = rewardSection.getString("type", "ITEM");
                    boolean isSeed = type.equalsIgnoreCase("SEED");

                    if(isSeed && !ConfigManager.getBoolean("씨앗_드랍여부")) {
                        continue;
                    }

                    if(isFortune) {
                        if(isSeed) {
                            int seedAmount = ThreadLocalRandom.current().nextInt(1, 3);
                            amount += seedAmount + getFortuneBonus(fortuneLevel);
                        } else {
                            amount += getFortuneBonus(fortuneLevel);
                        }
                    }

                    ItemStack awardItem = new ItemStack(material, amount);
                    ItemMeta meta = awardItem.getItemMeta();
                    if(meta != null) {
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rewardSection.getString("name")));
                        if(rewardSection.contains("custom_model_data")) {
                            meta.setCustomModelData(rewardSection.getInt("custom_model_data"));
                        }
                        List<String> lore = rewardSection.getStringList("lore");
                        if(lore != null) {
                            List<String> translatedLore = new ArrayList<>();
                            for(String line : lore) {
                                translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                            }
                            meta.setLore(translatedLore);
                        }

                        if (config.contains("crop_data")) {
                            NamespacedKey tags = new NamespacedKey(instance, "crop_data");
                            String cropData = config.getString("crop_data");
                            meta.getPersistentDataContainer().set(tags, PersistentDataType.STRING, cropData);
                        }

                        awardItem.setItemMeta(meta);
                    }
                    awards.add(awardItem);
                }
            }
        }
        return awards;
    }

    private static int getFortuneBonus(int fortuneLevel) {
        if (fortuneLevel <= 0) return 0;
        int seedAmount = ThreadLocalRandom.current().nextInt(1, fortuneLevel);
        return seedAmount;
    }
}