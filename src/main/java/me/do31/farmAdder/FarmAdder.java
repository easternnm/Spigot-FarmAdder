package me.do31.farmAdder;

import me.do31.farmAdder.commands.BasicCommand;
import me.do31.farmAdder.utils.ConfigManager;
import me.do31.farmAdder.utils.CropsConfigManager;
import me.do31.farmAdder.utils.DBManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FarmAdder extends JavaPlugin {
    private static FarmAdder instance;
    private static DBManager dbManager;

    public static FarmAdder getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.loadConfig();
        CropsConfigManager.loadCropsConfigs();

        dbManager = new DBManager("plugins/FarmAdder/FarmAdder.db");
        dbManager.setupDatabase(dbManager);

        getCommand("farmadder").setExecutor(new BasicCommand());
        this.getLogger().info("FarmAdder 가 정상적으로 활성화 되었습니다.");
    }

    @Override
    public void onDisable() {

        if(dbManager != null) {
            dbManager.close();
        }
        this.getLogger().info("FarmAdder 가 비활성화 되었습니다.");
    }
}
