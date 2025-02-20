package me.do31.farmAdder.listeners;

import me.do31.farmAdder.FarmAdder;
import me.do31.farmAdder.utils.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class OpenShopEvent implements Listener {
    private static FarmAdder instance = FarmAdder.getInstance();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String shopTitle = ChatColor.translateAlternateColorCodes('&', ShopManager.shopConfig.getString("gui.title"));

        if (!e.getView().getTitle().equals(shopTitle)) {
            return;
        }

        e.setCancelled(true);

        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        int slot = e.getSlot();

        Player player = (Player) e.getWhoClicked();

        if (e.getClick() == ClickType.LEFT) {
            ShopManager.sellItem(player, slot, false); // 이벤트 전달
        } else if (e.getClick() == ClickType.SHIFT_LEFT) {
            ShopManager.sellItem(player, slot, true); // 이벤트 전달
        }
    }
}