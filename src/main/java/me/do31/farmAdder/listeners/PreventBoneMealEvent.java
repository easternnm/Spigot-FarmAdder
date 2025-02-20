package me.do31.farmAdder.listeners;

import me.do31.farmAdder.FarmAdder;
import me.do31.farmAdder.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PreventBoneMealEvent implements Listener {

    private static final FarmAdder instance = FarmAdder.getInstance();

    @EventHandler
    public void onBoneMealUse(PlayerInteractEvent e) {
        // 플레이어가 블록을 우클릭하지 않으면 무시
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }

        // 플레이어가 사용한 아이템 가져오기
        ItemStack itemInHand = e.getItem();
        if (itemInHand == null || itemInHand.getType() != Material.BONE_MEAL) {
            return;
        }

        // 위치를 문자열로 변환하여 데이터베이스 조회
        String locString = StringUtils.locationToString(block.getLocation());
        List<String[]> results = instance.getDBManager().selectData("SELECT crop FROM crops WHERE location = ?", locString);

        // 특정 작물에 뼛가루 사용 차단
        if (!results.isEmpty() && !instance.getConfig().getBoolean("뼛가루_사용여부") && !e.getPlayer().isOp()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c⚠ 이 농작물에는 뼛가루를 사용할 수 없습니다. ⚠"));
        }
    }

    @EventHandler
    public void onBoneMealDispense(BlockDispenseEvent e) {
        ItemStack item = e.getItem();

        // 뼛가루인지 확인 & 설정에서 사용 가능 여부 확인
        if (item.getType() == Material.BONE_MEAL && !instance.getConfig().getBoolean("뼛가루_사용여부")) {
            Block dispenserBlock = e.getBlock();

            // 디스펜서인지 확인
            if (!(dispenserBlock.getState() instanceof Dispenser)) {
                return;
            }

            // 디스펜서의 방향 가져오기
            BlockFace facing = ((Directional) dispenserBlock.getBlockData()).getFacing();
            Block targetBlock = dispenserBlock.getRelative(facing);

            // 위치를 문자열로 변환하여 데이터베이스 조회
            String locString = StringUtils.locationToString(targetBlock.getLocation());
            List<String[]> results = instance.getDBManager().selectData("SELECT crop FROM crops WHERE location = ?", locString);

            // 특정 작물에 뼛가루 사용 차단
            if (!results.isEmpty() && !instance.getConfig().getBoolean("뼛가루_사용여부")) {
                e.setCancelled(true);
            }
        }
    }
}