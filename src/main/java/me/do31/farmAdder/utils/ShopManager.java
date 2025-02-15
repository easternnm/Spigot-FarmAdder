package me.do31.farmAdder.utils;

import me.do31.farmAdder.FarmAdder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class ShopManager {
    private static final FarmAdder instance = FarmAdder.getInstance();
    private static Economy economy;
    public static FileConfiguration shopConfig;

    public static void loadShopConfig() {

        File shopFile = new File(instance.getDataFolder(), "shop.yml");
        if(!shopFile.exists()) {
            instance.saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);

        if(!setupEconomy()) {
            instance.getLogger().severe("Vault 플러그인이 설치되어 있지 않습니다.");
            Bukkit.getPluginManager().disablePlugin(instance);
        }
    }

    public static void openShop(Player player) {
        Inventory shop = createShop(player);
        player.openInventory(shop);
    }

    private static Inventory createShop(Player player) {

        String title = ChatColor.translateAlternateColorCodes('&', shopConfig.getString("gui.title"));
        int size = shopConfig.getInt("gui.size");

        Inventory inventory = Bukkit.createInventory(null, size, title);
        ConfigurationSection itemsSection = shopConfig.getConfigurationSection("gui.items");

        if(itemsSection == null) {
            return inventory;
        }

        for(String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);

            int slot = Integer.parseInt(key);

            String id = itemSection.getString("id", "GRAY_STAINED_GLASS_PANE");
            String name = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name", ""));
            List<String> lore = new ArrayList<>(itemSection.getStringList("lore"));

            lore = StringUtils.replacePlaceholders(lore, itemSection, player);

            Material material = Material.matchMaterial(id);
            ItemStack item = new ItemStack(material);

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                meta.setLore(lore);

                if (itemSection.contains("custom_model_data")) {
                    meta.setCustomModelData(itemSection.getInt("custom_model_data", 0));
                }

                if (itemSection.contains("crop_data")) {
                    NamespacedKey tags = new NamespacedKey(instance, "crop_data");
                    String cropData = itemSection.getString("crop_data");
                    meta.getPersistentDataContainer().set(tags, PersistentDataType.STRING, cropData);
                }

                item.setItemMeta(meta);
            }

            if(slot < inventory.getSize()) {
                inventory.setItem(slot, item);
            }
        }
        return inventory;
    }

    private static boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public static void addMoney(Player player, double totalPrice) {
        economy.depositPlayer(player, totalPrice);
    }

    public static void sellItem(Player player, int key, boolean isShiftClick) {
        Inventory inventory = player.getInventory();
        ConfigurationSection itemSection = shopConfig.getConfigurationSection("gui.items." + key);
        int totalAmount = 0;

        if(!itemSection.contains("price")) {
            return;
        }

        double price = itemSection.getDouble("price");
        String name = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name"));

        NamespacedKey cropDataKey = new NamespacedKey(instance, "crop_data");

        for (ItemStack item : inventory.getContents()) {
            if (item == null || !item.hasItemMeta()) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();

            if (item.getType() != Material.matchMaterial(itemSection.getString("id"))) {
                continue;
            }

            if(itemSection.contains("custom_model_data")) {
                int customModelData = itemSection.getInt("custom_model_data");
                if (meta.getCustomModelData() != customModelData) {
                    continue;
                }
            }

            if(itemSection.contains("crop_data")) {
                if (meta.getPersistentDataContainer().has(cropDataKey, PersistentDataType.STRING)) {
                    String itemData = meta.getPersistentDataContainer().get(cropDataKey, PersistentDataType.STRING);
                    if (!itemSection.getString("crop_data").equals(itemData)) {
                        continue;
                    }
                }
            }

            totalAmount += item.getAmount();

            if(totalAmount == 0) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f]&c 인벤토리에 아이템이 존재하지 않습니다."));
                return;
            }

            if(!isShiftClick) {
                item.setAmount(item.getAmount() - 1);
                if(item.getAmount() == 0) {
                    inventory.remove(item);
                }
                addMoney(player, price);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] " + name +"를 1개 팔았습니다. 수익 " + price + "원"));
                return;
            } else if(isShiftClick) {
                inventory.remove(item);
                addMoney(player, price * totalAmount);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] " + name +"를 " + totalAmount + "개 팔았습니다. 수익 " + price * totalAmount + "원"));

            }
        }
    }
}
