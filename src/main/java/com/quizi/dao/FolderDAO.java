package com.quizi.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.quizi.dto.FolderDTO;
import com.quizi.util.DBConnection;

public class FolderDAO {

    // 폴더 생성
    public boolean createFolder(long userId, String name) {
        String sql = "INSERT INTO folders (user_id, name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, name);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 내 폴더 목록 조회
    public List<FolderDTO> selectByUserId(long userId) {
        List<FolderDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM folders WHERE user_id = ? ORDER BY created_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FolderDTO f = new FolderDTO();
                    f.setId(rs.getLong("id"));
                    f.setUserId(rs.getLong("user_id"));
                    f.setName(rs.getString("name"));
                    f.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(f);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 문제집 이동 (폴더 변경)
    public boolean moveWorkbook(long workbookId, String folderIdStr) {
        String sql = "UPDATE workbooks SET folder_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // folderIdStr가 "null"이거나 비어있으면 NULL로 설정 (전체 보기로 이동)
            if (folderIdStr == null || folderIdStr.isEmpty() || folderIdStr.equals("null")) {
                pstmt.setNull(1, Types.BIGINT);
            } else {
                pstmt.setLong(1, Long.parseLong(folderIdStr));
            }
            pstmt.setLong(2, workbookId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 폴더 삭제 (폴더 안의 문제집은 삭제되지 않고 folder_id가 NULL이 됨 - DB FK 설정에 따름)
    public boolean deleteFolder(long folderId) {
        String sql = "DELETE FROM folders WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, folderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}