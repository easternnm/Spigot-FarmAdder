package me.do31.farmAdder.listners;

import me.do31.farmAdder.FarmAdder;
import me.do31.farmAdder.utils.ConfigManager;
import me.do31.farmAdder.utils.CropsConfigManager;
import me.do31.farmAdder.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CropWaterBreakEvent implements Listener {

    private static final FarmAdder instance = FarmAdder.getInstance();

    @EventHandler
    public void onWaterBreak(BlockFromToEvent e) {
        Block toBlock = e.getToBlock();
        Block belowBlock = toBlock.getRelative(BlockFace.DOWN);


        if (toBlock.getType() != Material.AIR && belowBlock.getType() == Material.FARMLAND) {
            Block fromBlock = e.getBlock();

            if (fromBlock.getType() == Material.WATER) {
                Location loc = toBlock.getLocation();
                String locString = StringUtils.locationToString(loc);

                List<String[]> results = instance.getDBManager().selectData("SELECT crop FROM crops WHERE location = ?", locString);

                if (!results.isEmpty()) {
                    String cropType = results.get(0)[0];

                    if(!ConfigManager.getBoolean("물_농작물_수확")) {
                        return;
                    }


                    if (instance.getConfig().getBoolean("씨앗_드랍여부")) {
                        ItemStack seedItem = CropsConfigManager.createSeed(cropType);
                        if (seedItem != null) {
                            toBlock.getWorld().dropItemNaturally(loc, seedItem);
                        }
                    }

                    if (toBlock.getBlockData() instanceof Ageable) {
                        Ageable ageable = (Ageable) toBlock.getBlockData();
                        if (ageable.getAge() == ageable.getMaximumAge()) {
                            List<ItemStack> awards = CropsConfigManager.createAwards(cropType);
                            if (awards != null) {
                                for (ItemStack award : awards) {
                                    toBlock.getWorld().dropItemNaturally(loc, award);
                                }
                            }
                        }
                    }
                    instance.getDBManager().deleteData("DELETE FROM crops WHERE location = ?", locString);
                }
                e.setCancelled(true);
                toBlock.setType(Material.AIR);
            }
        }
    }
}
