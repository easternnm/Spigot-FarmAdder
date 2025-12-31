
package me.do31.farmAdder.listeners;

import me.do31.farmAdder.FarmAdder;
import me.do31.farmAdder.utils.ConfigManager;
import me.do31.farmAdder.utils.CropsConfigManager;
import me.do31.farmAdder.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Waterlogged;
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

            if (fromBlock.getType() == Material.WATER || (fromBlock.getBlockData() instanceof Waterlogged && ((Waterlogged) fromBlock.getBlockData()).isWaterlogged())) {
                Location loc = toBlock.getLocation();
                String locString = StringUtils.locationToString(loc);

                String cropType = instance.getCropLocations().get(locString);

                if (cropType != null) {
                    if(!ConfigManager.WATER_CROP_HARVEST) {
                        return;
                    }

                    if (ConfigManager.DROP_SEEDS) {
                        ItemStack seedItem = CropsConfigManager.createSeed(cropType);
                        if (seedItem != null) {
                            toBlock.getWorld().dropItemNaturally(loc, seedItem);
                        }
                    }

                    if (toBlock.getBlockData() instanceof Ageable) {
                        Ageable ageable = (Ageable) toBlock.getBlockData();
                        if (ageable.getAge() == ageable.getMaximumAge()) {
                            List<ItemStack> awards = CropsConfigManager.createAwards(cropType, 0);
                            if (awards != null) {
                                for (ItemStack award : awards) {
                                    toBlock.getWorld().dropItemNaturally(loc, award);
                                }
                            }
                        }
                    }
                    // DB에서 즉시 삭제하는 대신, 삭제 대기열에 추가
                    instance.queueDelete(locString);
                    // 캐시에서는 즉시 제거하여 게임 내 상태와 일치시킴
                    instance.removeCropFromCache(locString);
                    e.setCancelled(true);
                    toBlock.setType(Material.AIR);
                }
            }
        }
    }
}
