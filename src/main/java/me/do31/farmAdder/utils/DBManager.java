package me.do31.farmAdder.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private Connection connection;
    private final String dbUrl;

    // 생성자에서 DB 경로 설정
    public DBManager(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        connect();
    }

    // 연결 유지 및 자동 복구
    private void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(dbUrl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        connect(); // 항상 연결 유지
        return connection;
    }

    // 테이블 생성
    public void createTable(String tableName, String tableSchema) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableSchema + ")";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 데이터 삽입
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

    // 데이터 수정
    public int updateData(String query, Object... params) {
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 데이터 조회 (ResultSet이 아니라 리스트로 반환)
    public List<String[]> selectData(String query, Object... params) {
        List<String[]> results = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
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

    // 데이터 삭제 (WHERE 절 검증 추가)
    public void deleteData(String query, Object... params) {
        if (!query.toLowerCase().contains("where")) {
            throw new IllegalArgumentException("[DB] DELETE 실행 시 WHERE 조건이 필요합니다.");
        }
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
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

    // 연결 닫기
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void setupDatabase() {
        createTable("crops", "id INTEGER PRIMARY KEY, location TEXT, crop TEXT");
    }
}
