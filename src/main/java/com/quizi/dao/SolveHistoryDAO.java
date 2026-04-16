package com.quizi.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.quizi.util.DBConnection;

public class SolveHistoryDAO {
    // 사용자의 풀이 기록 목록 조회
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
                    map.put("id", rs.getLong("id")); // history_id
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

    // [수정] 결과 저장 (상세 내역 포함, 트랜잭션 적용)
    public long saveHistory(long userId, long workbookId, int earned, int total, int correct, int count, List<Map<String, Object>> details) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long historyId = 0;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. 메인 기록 저장
            String sqlHistory = "INSERT INTO solve_history (user_id, workbook_id, earned_score, total_score, correct_count, total_count) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sqlHistory, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, userId);
            pstmt.setLong(2, workbookId);
            pstmt.setInt(3, earned);
            pstmt.setInt(4, total);
            pstmt.setInt(5, correct);
            pstmt.setInt(6, count);
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            if (rs.next()) historyId = rs.getLong(1);
            else throw new SQLException("History ID creation failed");

            // 2. 상세 내역 저장
            String sqlDetail = "INSERT INTO solve_history_details (history_id, question_id, user_answer, is_correct) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmtDetail = conn.prepareStatement(sqlDetail)) {
                for (Map<String, Object> detail : details) {
                    pstmtDetail.setLong(1, historyId);
                    // DTO 대신 Map을 쓰고 있어서 형변환 필요 (GradeController에서 맞춰줄 예정)
                    pstmtDetail.setLong(2, (Long) detail.get("questionId"));
                    pstmtDetail.setString(3, (String) detail.get("userAnswer"));
                    pstmtDetail.setBoolean(4, (Boolean) detail.get("isCorrect"));
                    pstmtDetail.addBatch();
                }
                pstmtDetail.executeBatch();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            return 0;
        } finally {
            DBConnection.close(conn);
        }
        return historyId;
    }

    // [신규] 특정 기록의 상세 내역 조회
    public List<Map<String, Object>> selectHistoryDetails(long historyId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT shd.*, q.question_text, q.answer_text, q.explanation, q.score " +
                "FROM solve_history_details shd " +
                "JOIN questions q ON shd.question_id = q.id " +
                "WHERE shd.history_id = ? " +
                "ORDER BY q.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, historyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("questionText", rs.getString("question_text"));
                    map.put("answerText", rs.getString("answer_text"));
                    map.put("userAnswer", rs.getString("user_answer"));
                    map.put("explanation", rs.getString("explanation"));
                    map.put("score", rs.getInt("score"));
                    map.put("isCorrect", rs.getBoolean("is_correct"));
                    list.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}