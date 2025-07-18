package me.do31.farmAdder.listeners;

import me.do31.farmAdder.FarmAdder;
import me.do31.farmAdder.utils.ConfigManager;
import me.do31.farmAdder.utils.CropsConfigManager;
import me.do31.farmAdder.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CropBreakEvent implements Listener {

    private static final FarmAdder instance = FarmAdder.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHarvest(BlockBreakEvent e) {

        Block block = e.getBlock();
        Location loc = block.getLocation();
        String locString = StringUtils.locationToString(loc);

        Block upperBlock = block.getWorld().getBlockAt(loc.clone().add(0, 1, 0));
        Location locUpper = upperBlock.getLocation();
        String locUpperString = StringUtils.locationToString(locUpper);

        boolean isCropRemoved = false;

        // 위 블록이 작물인 경우
        if (upperBlock.getBlockData() instanceof Ageable) {
            String cropType = instance.getCropLocations().get(locUpperString);

            if (cropType != null) {
                Ageable ageable = (Ageable) upperBlock.getBlockData();

                Player player = e.getPlayer();
                ItemStack tool = player.getInventory().getItemInMainHand();
                int fortuneLevel = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS);

                // 아이템 드롭 처리
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    List<ItemStack> awards = CropsConfigManager.createAwards(cropType, fortuneLevel);
                    if (awards != null) {
                        for (ItemStack award : awards) {
                            upperBlock.getWorld().dropItemNaturally(locUpper, award);
                        }
                    }
                } else {
                    if (ConfigManager.DROP_SEEDS) {
                        ItemStack seedItem = CropsConfigManager.createSeed(cropType);
                        if (seedItem != null) {
                            upperBlock.getWorld().dropItemNaturally(locUpper, seedItem);
                        }
                    }
                }

                // 위 블록 제거 (작물만 삭제)
                upperBlock.setType(Material.AIR);
                isCropRemoved = true;

                // 데이터 삭제
                instance.getDBManager().deleteData("DELETE FROM crops WHERE location = ?", locUpperString);
                instance.getCropLocations().remove(locUpperString); // 캐시에서 제거
            }
        }

        // 원래 작물 블록을 부순 경우의 처리
        String cropType = instance.getCropLocations().get(locString);

        if (cropType != null) {
            if (block.getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) block.getBlockData();
                Player player = e.getPlayer();
                ItemStack tool = player.getInventory().getItemInMainHand();
                int fortuneLevel = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS);

                if (ageable.getAge() == ageable.getMaximumAge()) {
                    List<ItemStack> awards = CropsConfigManager.createAwards(cropType, fortuneLevel);
                    if (awards != null) {
                        for (ItemStack award : awards) {
                            block.getWorld().dropItemNaturally(loc, award);
                        }
                    }
                } else {
                    if (ConfigManager.DROP_SEEDS) {
                        ItemStack seedItem = CropsConfigManager.createSeed(cropType);
                        if (seedItem != null) {
                            block.getWorld().dropItemNaturally(loc, seedItem);
                        }
                    }
                }

                e.setDropItems(false); // 작물 블록은 드랍하지 않음
                instance.getDBManager().deleteData("DELETE FROM crops WHERE location = ?", locString);
                instance.getCropLocations().remove(locString); // 캐시에서 제거
            }
        }

        if (isCropRemoved) {
            e.setDropItems(true);
        }
    }
}