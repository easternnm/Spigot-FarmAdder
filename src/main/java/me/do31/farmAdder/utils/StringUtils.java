package me.do31.farmAdder.utils;

import me.do31.farmAdder.FarmAdder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
public class StringUtils {

    public static String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static Location stringToLocation(String locString) {
        if (locString == null || locString.isEmpty()) {
            return null;
        }
        String[] parts = locString.split(",");
        if (parts.length != 4) {
            return null;
        }
        World world = FarmAdder.getInstance().getServer().getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String chunkKey(Location location) {
        return chunkKey(location.getWorld().getName(), location.getBlockX(), location.getBlockZ());
    }

    public static String chunkKey(String locString) {
        if (locString == null || locString.isEmpty()) {
            return null;
        }
        String[] parts = locString.split(",");
        if (parts.length != 4) {
            return null;
        }
        try {
            String world = parts[0];
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[3]);
            return chunkKey(world, x, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String chunkKey(String world, int blockX, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        return world + ":" + chunkX + ":" + chunkZ;
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
