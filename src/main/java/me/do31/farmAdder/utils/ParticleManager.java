package me.do31.farmAdder.utils;

import me.do31.farmAdder.FarmAdder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;

public class ParticleManager {

    private static final FarmAdder instance = FarmAdder.getInstance();
    private static BukkitTask currentTask;
    private static final int MAX_PARTICLES_PER_TICK = 300;
    private static final int CHUNK_RADIUS = 1; // 플레이어 주변 3x3 청크만 검사

    public static void spawnParticle() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel();
        }

        currentTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ConfigManager.PARTICLE_ENABLED) {
                    return;
                }

                Particle particle = Particle.valueOf(ConfigManager.PARTICLE_TYPE);
                int amount = ConfigManager.PARTICLE_AMOUNT;
                double maxDistanceSquared = Math.pow(ConfigManager.PARTICLE_MAX_DISTANCE, 2);
                int spawned = 0;

                Map<String, Set<String>> chunkMap = instance.getCropsByChunk();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location playerLocation = player.getLocation();
                    String worldName = playerLocation.getWorld().getName();
                    int playerChunkX = playerLocation.getChunk().getX();
                    int playerChunkZ = playerLocation.getChunk().getZ();

                    for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
                        for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                            int chunkX = playerChunkX + dx;
                            int chunkZ = playerChunkZ + dz;

                            if (!playerLocation.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                                continue;
                            }

                            String chunkKey = worldName + ":" + chunkX + ":" + chunkZ;
                            Set<String> cropLocs = chunkMap.get(chunkKey);
                            if (cropLocs == null || cropLocs.isEmpty()) {
                                continue;
                            }

                            Chunk chunk = playerLocation.getWorld().getChunkAt(chunkX, chunkZ);
                            if (!chunk.isLoaded()) {
                                continue;
                            }

                            for (String locString : cropLocs) {
                                Location cropLocation = StringUtils.stringToLocation(locString);
                                if (cropLocation == null || !playerLocation.getWorld().equals(cropLocation.getWorld())) {
                                    continue;
                                }
                                if (playerLocation.distanceSquared(cropLocation) > maxDistanceSquared) {
                                    continue;
                                }

                                if (!(cropLocation.getBlock().getBlockData() instanceof Ageable)) {
                                    // 실제 블록이 사라진 경우 캐시/DB에서 정리
                                    instance.queueDelete(locString);
                                    instance.removeCropFromCache(locString);
                                    continue;
                                }

                                cropLocation.getWorld().spawnParticle(particle, cropLocation.clone().add(0.5, 1, 0.5), amount);
                                spawned++;

                                if (spawned >= MAX_PARTICLES_PER_TICK) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(instance, 0, 20L * Math.max(1, ConfigManager.PARTICLE_PERIOD));
    }
}
