package com.quizi.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {

    private static volatile HikariDataSource dataSource;

    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(ConfigManager.getProperty("db.url"));
        config.setUsername(ConfigManager.getProperty("db.username"));
        config.setPassword(ConfigManager.getProperty("db.password"));

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);  // 30초
        config.setIdleTimeout(600_000);       // 10분
        config.setMaxLifetime(1_800_000);     // 30분
        config.setPoolName("QuiziPool");
        config.setInitializationFailTimeout(-1); // 시작 시 DB 연결 실패해도 앱 구동 허용

        return new HikariDataSource(config);
    }

    private static HikariDataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DBConnection.class) {
                if (dataSource == null) {
                    dataSource = createDataSource();
                }
            }
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
