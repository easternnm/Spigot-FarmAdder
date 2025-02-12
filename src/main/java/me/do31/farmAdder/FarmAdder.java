package me.do31.farmAdder;

import me.do31.farmAdder.commands.BasicCommand;
import me.do31.farmAdder.commands.BasicCommandTabCompleter;
import me.do31.farmAdder.listners.CropBreakEvent;
import me.do31.farmAdder.listners.CropPlaceEvent;
import me.do31.farmAdder.listners.CropWaterBreakEvent;
import me.do31.farmAdder.utils.ConfigManager;
import me.do31.farmAdder.utils.CropsConfigManager;
import me.do31.farmAdder.utils.DBManager;
import me.do31.farmAdder.listners.PreventBoneMealEvent;
import me.do31.farmAdder.utils.ParticleManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FarmAdder extends JavaPlugin {
    private static FarmAdder instance;
    private final DBManager dbManager = new DBManager("plugins/FarmAdder/FarmAdder.db");

    public static FarmAdder getInstance() {
        return instance;
    }

    public static DBManager getDBManager() {
        return instance.dbManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.loadConfig();
        CropsConfigManager.loadCropsConfigs();

        dbManager.setupDatabase();

        getCommand("farmadder").setExecutor(new BasicCommand());
        getCommand("farmadder").setTabCompleter(new BasicCommandTabCompleter());

        getServer().getPluginManager().registerEvents(new CropPlaceEvent(), this);
        getServer().getPluginManager().registerEvents(new CropBreakEvent(), this);
        getServer().getPluginManager().registerEvents(new CropWaterBreakEvent(), this);
        getServer().getPluginManager().registerEvents(new PreventBoneMealEvent(), this);

        ParticleManager.spawnParticle();
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
