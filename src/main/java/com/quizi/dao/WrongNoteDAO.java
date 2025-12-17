package com.quizi.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.quizi.util.DBConnection;

public class WrongNoteDAO {

    // 1. 오답노트 저장 (중복 방지: INSERT IGNORE)
    public int saveWrongNotes(long userId, List<Map<String, Object>> notes) {
        String sql = "INSERT IGNORE INTO wrong_notes (user_id, question_id, user_answer) VALUES (?, ?, ?)";
        int count = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Map<String, Object> note : notes) {
                pstmt.setLong(1, userId);
                pstmt.setLong(2, Long.parseLong(note.get("questionId").toString()));
                pstmt.setString(3, (String) note.get("userAnswer"));
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int r : results) {
                if (r > 0) count++;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    // 2. 내 오답노트 목록 조회 (문제집 제목, 문제 정보 포함)
    public List<Map<String, Object>> selectByUserId(long userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT wn.*, q.question_text, q.answer_text, q.explanation, w.title as workbook_title " +
                "FROM wrong_notes wn " +
                "JOIN questions q ON wn.question_id = q.id " +
                "JOIN workbooks w ON q.workbook_id = w.id " +
                "WHERE wn.user_id = ? " +
                "ORDER BY wn.saved_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getLong("id"));
                    map.put("workbookTitle", rs.getString("workbook_title"));
                    map.put("questionText", rs.getString("question_text"));
                    map.put("answerText", rs.getString("answer_text"));
                    map.put("userAnswer", rs.getString("user_answer"));
                    map.put("explanation", rs.getString("explanation"));
                    map.put("savedAt", rs.getTimestamp("saved_at"));
                    list.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}