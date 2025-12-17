package com.quizi.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.quizi.util.DBConnection;

public class SolveHistoryDAO {
    // 사용자의 풀이 기록 조회 (문제집 제목 포함)
    public List<Map<String, Object>> selectByUserId(long userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sh.*, w.title as workbook_title " +
                "FROM solve_history sh " +
                "JOIN workbooks w ON sh.workbook_id = w.id " +
                "WHERE sh.user_id = ? " +
                "ORDER BY sh.solved_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("workbookId", rs.getLong("workbook_id"));
                    map.put("workbookTitle", rs.getString("workbook_title"));
                    map.put("earnedScore", rs.getInt("earned_score"));
                    map.put("totalScore", rs.getInt("total_score"));
                    map.put("correctCount", rs.getInt("correct_count"));
                    map.put("totalCount", rs.getInt("total_count"));
                    map.put("solvedAt", rs.getTimestamp("solved_at"));
                    list.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 결과 저장 (채점 후 호출용 - 미리 만들어 둠)
    public void saveHistory(long userId, long workbookId, int earned, int total, int correct, int count) {
        String sql = "INSERT INTO solve_history (user_id, workbook_id, earned_score, total_score, correct_count, total_count) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, workbookId);
            pstmt.setInt(3, earned);
            pstmt.setInt(4, total);
            pstmt.setInt(5, correct);
            pstmt.setInt(6, count);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}