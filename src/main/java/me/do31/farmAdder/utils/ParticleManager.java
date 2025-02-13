package me.do31.farmAdder.utils;

import me.do31.farmAdder.FarmAdder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParticleManager {

    private static final FarmAdder instance = FarmAdder.getInstance();
    private static BukkitTask currentTask;

    public static void spawnParticle() {
        if(currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel();
        }

        currentTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(ConfigManager.getBoolean("파티클_사용여부")) {
                    String particleType = ConfigManager.getString("파티클_종류");
                    int particleAmount = ConfigManager.getInt("파티클_갯수");
                    double maxDistance = ConfigManager.getInt("파티클_최대거리");
                    Particle particle = Particle.valueOf(particleType);

                    List<Location> cropLocations = getCropLocation();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Location playerLocation = player.getLocation();
                        for (Location location : cropLocations) {
                            if (location.getWorld().equals(playerLocation.getWorld()) && location.distance(playerLocation) < maxDistance) {
                            Location particleLocation = location.add(0.5, 1, 0.5);
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
