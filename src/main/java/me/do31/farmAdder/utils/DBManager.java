package me.do31.farmAdder.utils;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBManager {
    private static Connection connection;

    private final String dbUrl;

    public DBManager(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        try {
            connection = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl);
        }
        return connection;
    }

    public static void createTable(String tableName, String tableSchema) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableSchema + ")";
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void insertData(String tableName, String columns, String values, Object... params) {
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int updateData(String query, Object... params) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }

    public static void deleteData() {
    }

    public static void selectData() {
    }

    public static void close() {
        try {
            if(connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setupDatabase(DBManager dbManager) {
        dbManager.createTable("farm", "id INTEGER PRIMARY KEY, location TEXT, crop TEXT");
    };
}
