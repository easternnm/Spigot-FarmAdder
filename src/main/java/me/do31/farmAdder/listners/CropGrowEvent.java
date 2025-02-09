package me.do31.farmAdder.listners;

import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class CropGrowEvent implements Listener {

    public CropGrowEvent() {
    }

    @EventHandler
    public void onCropGrowEvent(BlockGrowEvent e) {
        Block crop = e.getBlock();

        if(crop.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) crop.getBlockData();
            if(ageable.getAge() == ageable.getMaximumAge()) {
            }
        }
    }
}
