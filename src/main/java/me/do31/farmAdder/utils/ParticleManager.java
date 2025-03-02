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

                    // 데이터베이스 삭제 최적화 (삭제할 위치를 한 번에 모아서 삭제)
                    Iterator<Location> iterator = cropLocations.iterator();
                    while (iterator.hasNext()) {
                        Location location = iterator.next();
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

                    // 최적화된 플레이어-작물 거리 계산
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Location playerLocation = player.getLocation();
                        Chunk playerChunk = playerLocation.getChunk();

                        List<Location> nearbyCrops = chunkMap.getOrDefault(playerChunk, Collections.emptyList());
                        for (Location location : nearbyCrops) {
                            if (playerLocation.getWorld().equals(location.getWorld()) &&
                                    playerLocation.distanceSquared(location) < maxDistanceSquared) {
                                Location particleLocation = location.clone().add(0.5, 1, 0.5);
                                location.getWorld().spawnParticle(particle, particleLocation, particleAmount);
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
