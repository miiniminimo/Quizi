package com.quizi.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(DRIVER);

            // [수정] 설정 파일에서 정보 가져오기
            String url = ConfigManager.getProperty("db.url");
            String user = ConfigManager.getProperty("db.username");
            String pass = ConfigManager.getProperty("db.password");

            conn = DriverManager.getConnection(url, user, pass);
            // System.out.println("[DBConnection] Database connected successfully."); // 로그가 너무 많으면 주석 처리
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection failed.");
            e.printStackTrace();
        }
        return conn;
    }

    public static void close(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}