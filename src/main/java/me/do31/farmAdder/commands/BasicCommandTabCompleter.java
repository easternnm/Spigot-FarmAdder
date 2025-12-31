package me.do31.farmAdder.commands;

import me.do31.farmAdder.utils.CropsConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BasicCommandTabCompleter implements TabCompleter {


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> complections = new ArrayList<>();

        if(!(sender instanceof Player)) {
            return complections;
        }

        if(args.length == 1) {
            List<String> commands = List.of("give", "particle", "reload", "vacuum", "shop", "bonemeal");
            complections.addAll(commands);
        } else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("give")) {
                complections.addAll(sender.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            } else if(args[0].equalsIgnoreCase("particle")) {
                complections.addAll(Arrays.asList("on", "off", "type", "amount", "period" , "distance"));
            } else if(args[0].equalsIgnoreCase("bonemeal")) {
                complections.addAll(Arrays.asList("on", "off"));
            }
        } else if(args.length == 3) {
            if(args[0].equalsIgnoreCase("give")) {
                complections.addAll(Arrays.asList(CropsConfigManager.getCrops()));
            } else if(args[0].equalsIgnoreCase("particle")) {
                if(args[1].equalsIgnoreCase("type")) {
                    complections.addAll(Arrays.stream(org.bukkit.Particle.values()).map(Enum::name).collect(Collectors.toList()));
                }
            }
        }
        return complections.stream().filter(s -> s.toLowerCase().startsWith(args[args.length  -1].toLowerCase())).collect(Collectors.toList());
    }
}
