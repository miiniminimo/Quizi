package com.quizi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.quizi.util.DBConnection;
import com.quizi.dto.UserDTO;

public class UserDAO {

    // 회원가입
    public int signUp(UserDTO user) {
        String sql = "INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, 'USER')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());

            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 이메일로 사용자 정보 조회 (로그인용)
    public UserDTO getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        UserDTO user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new UserDTO();
                    user.setId(rs.getLong("id"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    user.setRole(rs.getString("role")); // [추가됨] 권한 정보 매핑
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    // [신규] 모든 사용자 목록 조회 (관리자용)
    public List<UserDTO> getAllUsers() {
        List<UserDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                UserDTO u = new UserDTO();
                u.setId(rs.getLong("id"));
                u.setEmail(rs.getString("email"));
                u.setName(rs.getString("name"));
                u.setRole(rs.getString("role"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(u);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // [신규] 사용자 삭제 (관리자용)
    public boolean deleteUser(long userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}