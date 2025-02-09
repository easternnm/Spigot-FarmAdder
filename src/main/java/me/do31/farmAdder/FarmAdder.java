package me.do31.farmAdder;

import me.do31.farmAdder.commands.BasicCommand;
import me.do31.farmAdder.utils.ConfigManager;
import me.do31.farmAdder.utils.CropsConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FarmAdder extends JavaPlugin {
    private static FarmAdder instance;

    public static FarmAdder getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.loadConfig();
        CropsConfigManager.loadCropsConfigs();
        this.getLogger().info("FarmAdder 가 정상적으로 활성화 되었습니다.");
        getCommand("farmadder").setExecutor(new BasicCommand());

    }

    @Override
    public void onDisable() {
    }
}
