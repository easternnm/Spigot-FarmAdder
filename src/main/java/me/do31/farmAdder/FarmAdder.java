package me.do31.farmAdder;

import me.do31.farmAdder.commands.*;
import me.do31.farmAdder.listeners.*;
import me.do31.farmAdder.utils.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class FarmAdder extends JavaPlugin {
    private static FarmAdder instance;
    private DBManager dbManager;
    private final Map<String, String> cropLocations = new HashMap<>();
    private final ConcurrentLinkedQueue<String> waterBreakDeletionQueue = new ConcurrentLinkedQueue<>();
    private BukkitTask dbTask;

    public static FarmAdder getInstance() {
        return instance;
    }

    public DBManager getDBManager() {
        return dbManager;
    }

    public Map<String, String> getCropLocations() {
        return cropLocations;
    }

    public ConcurrentLinkedQueue<String> getWaterBreakDeletionQueue() {
        return waterBreakDeletionQueue;
    }

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.init(this);
        ConfigManager.loadConfig();

        dbManager = new DBManager();

        // Load all crop locations from DB into cache
        List<String[]> allCrops = dbManager.selectData("SELECT location, crop FROM crops");
        for (String[] cropData : allCrops) {
            cropLocations.put(cropData[0], cropData[1]);
        }
        getLogger().info(cropLocations.size() + "개의 작물 위치를 캐시에 저장했습니다.");

        getCommand("farmadder").setExecutor(new BasicCommand());
        getCommand("farmadder").setTabCompleter(new BasicCommandTabCompleter());

        getServer().getPluginManager().registerEvents(new CropPlaceEvent(), this);
        getServer().getPluginManager().registerEvents(new CropBreakEvent(), this);
        getServer().getPluginManager().registerEvents(new CropWaterBreakEvent(), this);
        getServer().getPluginManager().registerEvents(new PreventBoneMealEvent(), this);
        getServer().getPluginManager().registerEvents(new OpenShopEvent(), this);

        ParticleManager.spawnParticle();
        startDatabaseTask();
        this.getLogger().info("FarmAdder 가 정상적으로 활성화 되었습니다.");
    }

    @Override
    public void onDisable() {
        // Process any remaining items in the queue before shutting down
        processDeletionQueue();

        if (dbTask != null && !dbTask.isCancelled()) {
            dbTask.cancel();
        }
        if(dbManager != null) {
            dbManager.close();
        }
        this.getLogger().info("FarmAdder 가 비활성화 되었습니다.");
    }

    private void startDatabaseTask() {
        // 5초(100틱)마다 대기열을 확인하여 비동기적으로 DB에 삭제 요청
        dbTask = getServer().getScheduler().runTaskTimerAsynchronously(this, this::processDeletionQueue, 100L, 100L);
    }

    private void processDeletionQueue() {
        if (waterBreakDeletionQueue.isEmpty()) {
            return;
        }

        List<String> toDelete = new ArrayList<>();
        // 한 번에 처리할 양을 제한하여 서버 부하 분산 (선택적)
        int count = 0;
        while (!waterBreakDeletionQueue.isEmpty() && count < 1000) { // 한 번에 최대 1000개 처리
            toDelete.add(waterBreakDeletionQueue.poll());
            count++;
        }

        if (!toDelete.isEmpty()) {
            dbManager.deleteBatchData(toDelete);
        }
    }
}
