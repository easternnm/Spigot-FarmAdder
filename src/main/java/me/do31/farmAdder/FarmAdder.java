package me.do31.farmAdder;

import me.do31.farmAdder.commands.*;
import me.do31.farmAdder.listeners.*;
import me.do31.farmAdder.utils.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class FarmAdder extends JavaPlugin {
    private static FarmAdder instance;
    private DBManager dbManager;

    public static FarmAdder getInstance() {
        return instance;
    }

    public DBManager getDBManager() {
        return dbManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.loadConfig();

        dbManager = new DBManager();

        getCommand("farmadder").setExecutor(new BasicCommand());
        getCommand("farmadder").setTabCompleter(new BasicCommandTabCompleter());

        getServer().getPluginManager().registerEvents(new CropPlaceEvent(), this);
        getServer().getPluginManager().registerEvents(new CropBreakEvent(), this);
        getServer().getPluginManager().registerEvents(new CropWaterBreakEvent(), this);
        getServer().getPluginManager().registerEvents(new PreventBoneMealEvent(), this);
        getServer().getPluginManager().registerEvents(new OpenShopEvent(), this);

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
