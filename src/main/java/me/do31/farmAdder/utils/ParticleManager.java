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
                if (ConfigManager.PARTICLE_ENABLED) {
                    String particleType = ConfigManager.PARTICLE_TYPE;
                    int particleAmount = ConfigManager.PARTICLE_AMOUNT;
                    double maxDistanceSquared = Math.pow(ConfigManager.PARTICLE_MAX_DISTANCE, 2);
                    Particle particle = Particle.valueOf(particleType);

                    // ✅ 비동기적으로 작물 위치 가져오기 (렉 방지)
                    Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                        List<Location> cropLocations = getCropLocation();
                        List<String> locationsToDelete = new ArrayList<>();
                        Map<Chunk, List<Location>> chunkMap = new HashMap<>();

                        // ✅ 동기 청크 로드 방지 및 데이터 캐싱
                        for (Location location : cropLocations) {
                            Chunk chunk = location.getChunk();
                            if (!chunk.isLoaded()) continue; // 비로드 청크는 무시

                            Block block = location.getBlock();
                            if (!(block.getBlockData() instanceof Ageable)) {
                                locationsToDelete.add(StringUtils.locationToString(location));
                            } else {
                                chunkMap.computeIfAbsent(chunk, k -> new ArrayList<>()).add(location);
                            }
                        }

                        // ✅ 데이터베이스에서 한번에 삭제
                        if (!locationsToDelete.isEmpty()) {
                            instance.getDBManager().deleteData("DELETE FROM crops WHERE location IN (" +
                                            String.join(",", Collections.nCopies(locationsToDelete.size(), "?")) + ")",
                                    locationsToDelete.toArray());
                        }

                        // ✅ 메인 스레드에서 플레이어 검사 실행
                        Bukkit.getScheduler().runTask(instance, () -> {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                Location playerLocation = player.getLocation();
                                Chunk playerChunk = playerLocation.getChunk();

                                for (int dx = -1; dx <= 1; dx++) {
                                    for (int dz = -1; dz <= 1; dz++) {
                                        Chunk nearbyChunk = playerChunk.getWorld().getChunkAt(playerChunk.getX() + dx, playerChunk.getZ() + dz);

                                        if (chunkMap.containsKey(nearbyChunk)) {
                                            for (Location location : chunkMap.get(nearbyChunk)) {
                                                if (playerLocation.getWorld().equals(location.getWorld()) &&
                                                        playerLocation.distanceSquared(location) < maxDistanceSquared) {
                                                    Location particleLocation = location.clone().add(0.5, 1, 0.5);
                                                    location.getWorld().spawnParticle(particle, particleLocation, particleAmount);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    });
                }
            }
        }.runTaskTimer(instance, 0, 20L * ConfigManager.PARTICLE_PERIOD);
    }



    public static List<Location> getCropLocation() {
        List<Location> locations = new ArrayList<>();
        try(Connection connection = instance.getDBManager().getConnection()) {
            String query = "SELECT location FROM crops";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                String locationString = resultSet.getString("location");
                if(locationString != null) {
                    String[] locationSplit = locationString.split(",");
                    Location location = new Location(instance.getServer().getWorld(locationSplit[0]), Double.parseDouble(locationSplit[1]), Double.parseDouble(locationSplit[2]), Double.parseDouble(locationSplit[3]));
                    locations.add(location);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locations;
    }
}