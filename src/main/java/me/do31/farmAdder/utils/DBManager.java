package me.do31.farmAdder.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBManager {

    private final HikariDataSource dataSource;

    public DBManager() {
        HikariConfig config = new HikariConfig();
        String dbType = ConfigManager.getString("database.type");

        if (dbType.equalsIgnoreCase("mysql")) {
            config.setJdbcUrl("jdbc:mysql://" + ConfigManager.getString("database.address") + ":" + ConfigManager.getInt("database.port") + "/" + ConfigManager.getString("database.database"));
            config.setUsername(ConfigManager.getString("database.username"));
            config.setPassword(ConfigManager.getString("database.password"));
        } else { // Default to SQLite
            config.setJdbcUrl("jdbc:sqlite:" + ConfigManager.getString("database.file"));
        }

        config.setMaximumPoolSize(ConfigManager.getInt("database.pool-size", 10));
        config.setConnectionTimeout(ConfigManager.getInt("database.connection-timeout", 30000));
        config.setIdleTimeout(ConfigManager.getInt("database.idle-timeout", 600000));
        config.setMaxLifetime(ConfigManager.getInt("database.max-lifetime", 1800000));

        this.dataSource = new HikariDataSource(config);
        setupDatabase();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public void setupDatabase() {
        createTable("crops", "id INTEGER PRIMARY KEY AUTOINCREMENT, location TEXT, crop TEXT");
    }

    public void createTable(String tableName, String tableSchema) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableSchema + ")";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertData(String tableName, String columns, String values, Object... params) {
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int updateData(String query, Object... params) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<String[]> selectData(String query, Object... params) {
        List<String[]> results = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    String[] row = new String[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = rs.getString(i + 1);
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public void deleteData(String query, Object... params) {
        if (!query.toLowerCase().contains("where")) {
            throw new IllegalArgumentException("[DB] DELETE 실행 시 WHERE 조건이 필요합니다.");
        }
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteBatchData(List<String> locations) {
        if (locations == null || locations.isEmpty()) {
            return;
        }

        String query = "DELETE FROM crops WHERE location IN (" + String.join(",", Collections.nCopies(locations.size(), "?")) + ")";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < locations.size(); i++) {
                stmt.setString(i + 1, locations.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertOrReplaceBatch(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        String dbType = ConfigManager.getString("database.type");
        String sql;

        if ("mysql".equalsIgnoreCase(dbType)) {
            sql = "INSERT INTO crops (location, crop) VALUES (?, ?) ON DUPLICATE KEY UPDATE crop = VALUES(crop)";
        } else {
            sql = "INSERT OR REPLACE INTO crops (location, crop) VALUES (?, ?)";
        }

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String[] row : rows) {
                if (row.length < 2) {
                    continue;
                }
                stmt.setString(1, row[0]);
                stmt.setString(2, row[1]);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertOrUpdateData(String query, Object... params) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
