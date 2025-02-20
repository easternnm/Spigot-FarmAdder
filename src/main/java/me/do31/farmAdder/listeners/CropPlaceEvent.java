package me.do31.farmAdder.listeners;

import me.do31.farmAdder.FarmAdder;
import me.do31.farmAdder.utils.CropsConfigManager;
import me.do31.farmAdder.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CropPlaceEvent implements Listener {

    private static final FarmAdder instance = FarmAdder.getInstance();

    private static final Set<Material> INTERACTIVE_BLOCKS = new HashSet<>(Arrays.asList(
            Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE, Material.BLAST_FURNACE,
            Material.SMOKER, Material.CRAFTING_TABLE, Material.ENCHANTING_TABLE, Material.ANVIL,
            Material.GRINDSTONE, Material.LECTERN, Material.BREWING_STAND, Material.BARREL,
            Material.SHULKER_BOX, Material.ENDER_CHEST, Material.HOPPER, Material.CARTOGRAPHY_TABLE,
            Material.LOOM, Material.SMITHING_TABLE, Material.STONECUTTER
    ));

    @EventHandler
    public void onPlant(PlayerInteractEvent e) {

        if(e.isCancelled()) {
            return;
        }

        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null) {

            Block clickedBlock = e.getClickedBlock();
            BlockFace face = e.getBlockFace();

            Block placedBlock = clickedBlock.getRelative(face);
            Location placedLocation = placedBlock.getLocation();

            Block belowBlock = placedBlock.getRelative(BlockFace.DOWN);

            if (INTERACTIVE_BLOCKS.contains(clickedBlock.getType()) && !e.getPlayer().isSneaking()) {
                return;
            }

            if(placedBlock.getType() != Material.AIR) {
                return;
            }

            ItemStack item = e.getItem();
            if(item.getItemMeta() == null) {
                return;
            }

            if(!item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(instance, "crop_data"), PersistentDataType.STRING)) {
                return;
            }

            String cropData = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(instance, "crop_data"), PersistentDataType.STRING);

            String locString = StringUtils.locationToString(placedLocation);

            if (belowBlock.getType() == Material.FARMLAND) {
                instance.getDBManager().insertOrUpdateData(
                        "INSERT OR REPLACE INTO crops (location, crop) VALUES (?, ?)",
                        locString, cropData
                );
            }
        }
    }
}