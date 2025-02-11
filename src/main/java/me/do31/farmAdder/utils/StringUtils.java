package me.do31.farmAdder.utils;

import org.bukkit.Location;

public class StringUtils {

    public static String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
}
