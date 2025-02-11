package me.do31.farmAdder.listners;

import me.do31.farmAdder.FarmAdder;
import me.do31.farmAdder.utils.ConfigManager;
import me.do31.farmAdder.utils.CropsConfigManager;
import me.do31.farmAdder.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CropBreakEvent implements Listener {

    private static final FarmAdder instance = FarmAdder.getInstance();

    @EventHandler
    public void onHarvest(BlockBreakEvent e) {
        Block block = e.getBlock();
        Location loc = block.getLocation();
        String locString = StringUtils.locationToString(loc);

        List<String[]> results = instance.getDBManager().selectData("SELECT crop FROM crops WHERE location = ?", locString);

        if (!results.isEmpty()) {
            String cropType = results.get(0)[0];

            if (ConfigManager.getBoolean("씨앗_드랍여부")) {
                ItemStack seedItem = CropsConfigManager.createSeed(cropType);
                if (seedItem != null) {
                    block.getWorld().dropItemNaturally(loc, seedItem);
                }
            }

            if (block.getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) block.getBlockData();
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    List<ItemStack> awards = CropsConfigManager.createAwards(cropType);
                    if (awards != null) {
                        for (ItemStack award : awards) {
                            block.getWorld().dropItemNaturally(loc, award);
                        }
                    }
                }
            }

            e.setDropItems(false);
            instance.getDBManager().deleteData("DELETE FROM crops WHERE location = ?", locString);
        }
    }
}