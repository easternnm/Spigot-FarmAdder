package me.do31.farmAdder.listners;

import me.do31.farmAdder.utils.CropsConfigManager;
import me.do31.farmAdder.utils.DBManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CropPlaceEvent implements Listener {

    public CropPlaceEvent() {
    }

    @EventHandler
    public void onPlant(PlayerInteractEvent e) {
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null) {
            ItemStack item = e.getItem();
            if(item.getItemMeta().hasCustomModelData()) {
                String cropType = CropsConfigManager.getCropType(item);
                if (cropType.equals(item.getType().toString())) {


                    e.setCancelled(false);
                }
            }
        }

    }
}
