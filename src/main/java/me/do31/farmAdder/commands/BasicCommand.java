package me.do31.farmAdder.commands;

import me.do31.farmAdder.FarmAdder;
import me.do31.farmAdder.utils.ConfigManager;
import me.do31.farmAdder.utils.CropsConfigManager;
import me.do31.farmAdder.utils.ParticleManager;
import me.do31.farmAdder.utils.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BasicCommand implements CommandExecutor {
    FarmAdder instance = FarmAdder.getInstance();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        boolean explain;

        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "[FarmAdder] 버킷에서는 사용할 수 없는 명령어 입니다.");
            return true;
        }

        if(!sender.hasPermission("farmadder.admin")) {
            sendCommandInfo((Player) sender, true);
            return true;
        }

        if(args.length == 0) {
            sendCommandInfo((Player) sender, true);
            return true;
        }

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("shop")) {
                ShopManager.openShop((Player) sender);
                return true;
            }
            if(args[0].equalsIgnoreCase("reload")) {
                if(sender.hasPermission("farmadder.admin")) {
                    ConfigManager.reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[&6 FarmAdder&f ]&e 플러그인 설정을 다시 불러왔습니다."));
                    return true;
                }
            }
        }
        if(args[0].equalsIgnoreCase("bonemeal")) {
            if(args[1].equalsIgnoreCase("on")) {
                ConfigManager.setBoolean("뼛가루_사용여부", true);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] 뼛가루 사용이 활성화 되었습니다."));
            } else if(args[1].equalsIgnoreCase("off")) {
                ConfigManager.setBoolean("뼛가루_사용여부", false);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] 뼛가루 사용이 비활성화 되었습니다."));
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("give")) {
            giveCommand((Player) sender, args);
            return true;
        }

        if(args[0].equalsIgnoreCase("particle")) {
            particleCommand((Player) sender, args);
            return true;
        } else {
            explain = true;
        }
        sendCommandInfo((Player) sender, explain);
        return true;
    }

    public void sendCommandInfo(Player player, Boolean explain) {
        if (explain) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f---------[ &6FarmAdder v" + instance.getDescription().getVersion() + " &f]---------"));
            player.sendMessage("");
            if(player.hasPermission("farmadder.admin")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/farmadder give <플레이어> <작물> <수량> "));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/farmadder shop"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/farmadder particle"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/farmadder bonemeal <on/off>"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/farmadder reload"));
                player.sendMessage("");
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Plugin by &6DEOJI_"));
            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&f----------------------------------"));
        }
    }

    public void particleInfo(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f---------[ &6FarmAdder v" + instance.getDescription().getVersion() + " &f]---------"));
        player.sendMessage("");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/farmadder particle <on/off>"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/farmadder particle amount <크기>"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/farmadder particle type <파티클 종류>"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/farmadder particle period <초>"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/farmadder particle distance <거리>"));
        player.sendMessage("");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Plugin by &6DEOJI_"));
        player.sendMessage("");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f----------------------------------"));
    }

    public void giveCommand(Player player, String[] args) {
        Player target = null;
        String cropName = "";
        int amount = 0;

        if (args.length == 3) {
            target = player;
            cropName = args[1];
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "잘못된 값입니다. 숫자만 입력해주세요.");
                return;
            }
        } else if(args.length == 4) {
            target = player.getServer().getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
                return;
            }
            cropName = args[2];
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "잘못된 값입니다. 숫자만 입력해주세요.");
                return;
            }
        } else {
            StringBuilder cropList = new StringBuilder(ChatColor.GREEN + "사용 가능한 작물: ");
            for (String crop : CropsConfigManager.getCrops()) {
                cropList.append(ChatColor.GRAY).append(crop).append(ChatColor.WHITE).append(", ");
            }
            player.sendMessage(cropList.toString());
            return;
        }

        ItemStack seedItem = CropsConfigManager.createSeed(cropName);
        if(seedItem == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 작물입니다.");
            return;
        }

        seedItem.setAmount(amount);
        target.getInventory().addItem(seedItem);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] " + target.getName() + "님에게 " + cropName + " " + amount + "개를 지급하였습니다."));
    }

    public void particleCommand(Player player, String[] args) {
        if(args.length == 1) {
            particleInfo(player);
        } else if(args.length == 2) {
            if(args[1].equalsIgnoreCase("on")) {
                ConfigManager.setBoolean("파티클_사용여부", true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] 파티클이 활성화 되었습니다."));
            } else if(args[1].equalsIgnoreCase("off")) {
                ConfigManager.setBoolean("파티클_사용여부", false);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] 파티클이 비활성화 되었습니다."));
            } else {
                particleInfo(player);
            }
        } else if(args.length == 3) {
            if(args[1].equalsIgnoreCase("amount")) {
                try {
                    int amount = Integer.parseInt(args[2]);
                    ConfigManager.setInt("파티클_갯수", amount);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] 파티클 갯수가 " + amount + "개로 변경되었습니다."));
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "잘못된 값입니다. 숫자만 입력해주세요.");
                }
            } else if(args[1].equalsIgnoreCase("type")) {
                try {
                    Particle particleType = Particle.valueOf(args[2].toUpperCase());
                    ConfigManager.setString("파티클_종류", particleType.name());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] 파티클 종류가 " + particleType.name() + "로 변경되었습니다."));

                } catch (IllegalAccessError e) {
                    player.sendMessage(ChatColor.RED + "잘못된 값입니다.");
                }
            } else if(args[1].equalsIgnoreCase("period")) {
                try {
                    int period = Integer.parseInt(args[2]);
                    ConfigManager.setInt("파티클_주기", period);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] 파티클 주기가 " + period + "초로 변경되었습니다."));

                    ParticleManager.spawnParticle();

                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "잘못된 값입니다. 숫자만 입력해주세요.");
                }
            } else if(args[1].equalsIgnoreCase("distance")) {
                try {
                    int distance = Integer.parseInt(args[2]);
                    ConfigManager.setInt("파티클_최대거리", distance);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[ &6FarmAdder &f] 파티클 최대거리가 " + distance + "m로 변경되었습니다."));
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "잘못된 값입니다. 숫자만 입력해주세요.");
                }
            } else {
                particleInfo(player);
            }
        } else {
            particleInfo(player);
        }
    }
}