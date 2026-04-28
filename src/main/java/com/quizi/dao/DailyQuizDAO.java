package com.quizi.dao;

import com.quizi.dto.QuestionDTO;
import com.quizi.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 매일 Slack으로 전송된 문제를 daily_quiz_log 테이블에 기록하고 조회합니다.
 *
 * 필요한 DDL:
 * CREATE TABLE daily_quiz_log (
 *   id          BIGINT AUTO_INCREMENT PRIMARY KEY,
 *   question_id BIGINT NOT NULL,
 *   sent_date   DATE   NOT NULL,
 *   UNIQUE KEY uq_sent_date (sent_date),
 *   FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
 * );
 */
public class DailyQuizDAO {

    /**
     * 오늘 날짜로 문제 ID를 기록합니다. 이미 오늘 기록이 있으면 덮어씁니다.
     */
    public void logTodayQuestion(long questionId) {
        String sql = "INSERT INTO daily_quiz_log (question_id, sent_date) VALUES (?, CURDATE()) " +
                     "ON DUPLICATE KEY UPDATE question_id = VALUES(question_id)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, questionId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("[DailyQuizDAO] 오늘의 문제 기록 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 오늘 날짜에 기록된 문제를 반환합니다. 없으면 null.
     * QuestionDTO.workbookTitle에 문제집 이름이 채워져 있습니다.
     */
    public QuestionDTO getTodayQuestion() {
        String sql = "SELECT q.*, w.title AS workbook_title " +
                     "FROM daily_quiz_log dql " +
                     "JOIN questions q ON dql.question_id = q.id " +
                     "JOIN workbooks w ON q.workbook_id = w.id " +
                     "WHERE dql.sent_date = CURDATE()";
        String sqlOpt = "SELECT option_text FROM question_options " +
                        "WHERE question_id = ? ORDER BY option_order ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                QuestionDTO q = new QuestionDTO();
                q.setId(rs.getLong("id"));
                q.setWorkbookId(rs.getLong("workbook_id"));
                q.setQuestionText(rs.getString("question_text"));
                q.setQuestionType(rs.getString("question_type"));
                q.setScore(rs.getInt("score"));
                q.setAnswerText(rs.getString("answer_text"));
                q.setExplanation(rs.getString("explanation"));
                q.setWorkbookTitle(rs.getString("workbook_title"));

                if ("multiple".equals(q.getQuestionType())) {
                    List<String> options = new ArrayList<>();
                    try (PreparedStatement pstmtOpt = conn.prepareStatement(sqlOpt)) {
                        pstmtOpt.setLong(1, q.getId());
                        try (ResultSet rsOpt = pstmtOpt.executeQuery()) {
                            while (rsOpt.next()) options.add(rsOpt.getString("option_text"));
                        }
                    }
                    q.setOptions(options);
                }
                return q;
            }
        } catch (Exception e) {
            System.err.println("[DailyQuizDAO] 오늘의 문제 조회 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
