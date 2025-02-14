package me.do31.farmAdder.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    public static String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static List<String> replacePlaceholders(List<String> lore, ConfigurationSection itemSection, Player player) {
        List<String> replacedLore = new ArrayList<>();

        for(String line: lore) {

            String replacedLine = line;

            if(line.contains("{price}")) {
                String price = itemSection.getString("price");
                replacedLine = replacedLine.replace("{price}", price);
            }

            if(line.contains("{balance}")) {
                double balance = ShopManager.getBalance(player);
                replacedLine = replacedLine.replace("{balance}", String.format("%.2f", balance));
            }

            replacedLore.add(ChatColor.translateAlternateColorCodes('&', replacedLine));
        }

        return replacedLore;
    }
}
