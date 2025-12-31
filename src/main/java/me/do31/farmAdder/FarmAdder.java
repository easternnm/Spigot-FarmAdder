package me.do31.farmAdder;

import me.do31.farmAdder.commands.*;
import me.do31.farmAdder.listeners.*;
import me.do31.farmAdder.utils.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

public final class FarmAdder extends JavaPlugin {
    private static FarmAdder instance;
    private DBManager dbManager;
    private final Map<String, String> cropLocations = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> cropsByChunk = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String[]> insertQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> deleteQueue = new ConcurrentLinkedQueue<>();
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

    public Map<String, Set<String>> getCropsByChunk() {
        return cropsByChunk;
    }

    public void queueInsert(String location, String crop) {
        insertQueue.add(new String[]{location, crop});
    }

    public void queueDelete(String location) {
        deleteQueue.add(location);
    }

    public void addCropToCache(String location, String crop) {
        cropLocations.put(location, crop);
        String chunkKey = StringUtils.chunkKey(location);
        if (chunkKey != null) {
            cropsByChunk.computeIfAbsent(chunkKey, k -> ConcurrentHashMap.newKeySet()).add(location);
        }
    }

    public void removeCropFromCache(String location) {
        cropLocations.remove(location);
        String chunkKey = StringUtils.chunkKey(location);
        if (chunkKey != null) {
            Set<String> set = cropsByChunk.get(chunkKey);
            if (set != null) {
                set.remove(location);
                if (set.isEmpty()) {
                    cropsByChunk.remove(chunkKey);
                }
            }
        }
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
            addCropToCache(cropData[0], cropData[1]);
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
        processQueues();

        if (dbTask != null && !dbTask.isCancelled()) {
            dbTask.cancel();
        }
        if(dbManager != null) {
            dbManager.close();
        }
        this.getLogger().info("FarmAdder 가 비활성화 되었습니다.");
    }

    private void startDatabaseTask() {
        // 주기적으로 대기열을 확인하여 비동기적으로 DB에 반영
        dbTask = getServer().getScheduler().runTaskTimerAsynchronously(this, this::processQueues, 20L, 20L);
    }

    private void processQueues() {
        List<String[]> toInsert = new ArrayList<>();
        List<String> toDelete = new ArrayList<>();

        int count = 0;
        while (!insertQueue.isEmpty() && count < 1000) {
            String[] row = insertQueue.poll();
            if (row != null) {
                toInsert.add(row);
            }
            count++;
        }

        count = 0;
        while (!deleteQueue.isEmpty() && count < 2000) {
            String loc = deleteQueue.poll();
            if (loc != null) {
                toDelete.add(loc);
            }
            count++;
        }

        if (!toInsert.isEmpty()) {
            dbManager.insertOrReplaceBatch(toInsert);
        }
        if (!toDelete.isEmpty()) {
            dbManager.deleteBatchData(toDelete);
        }
    }
}
