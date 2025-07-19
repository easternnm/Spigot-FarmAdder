package me.do31.farmAdder.utils;

import me.do31.farmAdder.FarmAdder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ParticleManager {

    private static final FarmAdder instance = FarmAdder.getInstance();
    private static BukkitTask currentTask;

    public static void spawnParticle() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel();
        }

        currentTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ConfigManager.PARTICLE_ENABLED || instance.getCropLocations().isEmpty()) {
                    return;
                }

                // 1. 유효한 작물 위치와 삭제할 유령 데이터를 선별
                Map<String, String> currentCrops = new HashMap<>(instance.getCropLocations());
                List<String> validLocations = new ArrayList<>();
                List<String> locationsToRemove = new ArrayList<>();

                // Bukkit API는 메인 스레드에서만 호출해야 하므로, 스케줄러를 통해 동기적으로 블록 상태를 확인
                Bukkit.getScheduler().runTask(instance, () -> {
                    for (String locString : currentCrops.keySet()) {
                        Location loc = StringUtils.stringToLocation(locString);
                        if (loc != null && loc.isWorldLoaded() && loc.getChunk().isLoaded()) {
                            Block block = loc.getBlock();
                            // 작물이 아니거나, 공기 블록이면 유령 데이터로 간주
                            if (!(block.getBlockData() instanceof Ageable)) {
                                locationsToRemove.add(locString);
                            } else {
                                validLocations.add(locString);
                            }
                        } else {
                            // 월드가 언로드되었거나 위치 정보가 잘못된 경우도 삭제 대상
                            locationsToRemove.add(locString);
                        }
                    }

                    // 2. 유령 데이터 정리 작업 수행
                    if (!locationsToRemove.isEmpty()) {
                        for (String locToRemove : locationsToRemove) {
                            // 캐시에서 즉시 제거
                            instance.getCropLocations().remove(locToRemove);
                            // DB 삭제 대기열에 추가
                            instance.getWaterBreakDeletionQueue().add(locToRemove);
                        }
                    }

                    // 3. 유효한 위치에만 파티클 생성
                    if (validLocations.isEmpty()) {
                        return;
                    }

                    Particle particle = Particle.valueOf(ConfigManager.PARTICLE_TYPE);
                    int particleAmount = ConfigManager.PARTICLE_AMOUNT;
                    double maxDistanceSquared = Math.pow(ConfigManager.PARTICLE_MAX_DISTANCE, 2);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Location playerLocation = player.getLocation();
                        for (String locString : validLocations) {
                            Location cropLocation = StringUtils.stringToLocation(locString);
                            if (cropLocation != null && playerLocation.getWorld().equals(cropLocation.getWorld()) && playerLocation.distanceSquared(cropLocation) < maxDistanceSquared) {
                                Location particleLocation = cropLocation.clone().add(0.5, 0.7, 0.5);
                                player.spawnParticle(particle, particleLocation, particleAmount, 0, 0, 0, 0);
                            }
                        }
                    }
                });
            }
        }.runTaskTimerAsynchronously(instance, 0, 20L * ConfigManager.PARTICLE_PERIOD);
    }
}
