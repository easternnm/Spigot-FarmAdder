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
                if (ConfigManager.getBoolean("파티클_사용여부")) {
                    String particleType = ConfigManager.getString("파티클_종류");
                    int particleAmount = ConfigManager.getInt("파티클_갯수");
                    double maxDistanceSquared = Math.pow(ConfigManager.getInt("파티클_최대거리"), 2);
                    Particle particle = Particle.valueOf(particleType);

                    List<Location> cropLocations = getCropLocation();
                    List<String> locationsToDelete = new ArrayList<>();
                    Map<Chunk, List<Location>> chunkMap = new HashMap<>();

                    // ✅ 동기 청크 로드 방지
                    Iterator<Location> iterator = cropLocations.iterator();
                    while (iterator.hasNext()) {
                        Location location = iterator.next();

                        if (!location.getChunk().isLoaded()) {
                            continue; // 비로드 청크 무시
                        }

                        Block block = location.getBlock();
                        if (!(block.getBlockData() instanceof Ageable)) {
                            locationsToDelete.add(StringUtils.locationToString(location));
                            iterator.remove();
                        } else {
                            chunkMap.computeIfAbsent(location.getChunk(), k -> new ArrayList<>()).add(location);
                        }
                    }

                    if (!locationsToDelete.isEmpty()) {
                        instance.getDBManager().deleteData("DELETE FROM crops WHERE location IN (" +
                                        String.join(",", Collections.nCopies(locationsToDelete.size(), "?")) + ")",
                                locationsToDelete.toArray());
                    }

                    // ✅ 플레이어 주변 청크 저장
                    Map<UUID, Set<Chunk>> playerChunks = new HashMap<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Chunk baseChunk = player.getLocation().getChunk();
                        Set<Chunk> nearbyChunks = new HashSet<>();

                        // 주변 3x3 청크까지 추가 (자신 포함 9청크)
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                Chunk nearbyChunk = baseChunk.getWorld().getChunkAt(baseChunk.getX() + dx, baseChunk.getZ() + dz);
                                nearbyChunks.add(nearbyChunk);
                            }
                        }
                        playerChunks.put(player.getUniqueId(), nearbyChunks);
                    }

                    // ✅ 플레이어 근처 청크의 작물만 확인 & 거리 필터링 추가
                    for (Map.Entry<Chunk, List<Location>> entry : chunkMap.entrySet()) {
                        Chunk cropChunk = entry.getKey();
                        List<Location> cropLocationsList = entry.getValue();

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (playerChunks.getOrDefault(player.getUniqueId(), Collections.emptySet()).contains(cropChunk)) {
                                Location playerLocation = player.getLocation();

                                for (Location location : cropLocationsList) {
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
            }
        }.runTaskTimer(instance, 0, 20L * ConfigManager.getInt("파티클_주기"));
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
