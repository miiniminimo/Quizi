package com.quizi.dao;

import com.quizi.dto.QuestionDTO;
import com.quizi.util.DBConnection;

import java.sql.*;
import java.util.*;

/**
 * 유저별 오늘의 문제 기능을 담당하는 DAO.
 *
 * 테이블 의존:
 *   daily_quiz_log     — 유저×날짜별 문제 배정
 *   daily_quiz_answers — 유저의 답변 기록
 *   daily_quiz_topic   — 유저의 주제 설정
 */
public class DailyQuizDAO {

    // ──────────────────────────────────────────────
    // 오늘의 문제 조회 / 생성 (lazy)
    // ──────────────────────────────────────────────

    /**
     * 오늘 유저에게 배정된 문제를 반환합니다.
     * 아직 배정되지 않았으면 주제를 참고해 무작위로 선택 후 로그에 기록합니다.
     *
     * @return {logId, question(QuestionDTO)} 또는 빈 Map (DB에 문제 없음)
     */
    public Map<String, Object> getOrCreateTodayEntry(long userId) {
        // 오늘 이미 배정된 항목이 있으면 바로 반환
        Map<String, Object> existing = getTodayLogEntry(userId);
        if (existing != null) return existing;

        // 없으면 새로 배정
        String topic = getUserTopic(userId);
        QuestionDTO q = pickQuestion(userId, topic);
        if (q == null) return Collections.emptyMap();

        long logId = insertLog(userId, q.getId());
        if (logId <= 0) return Collections.emptyMap();

        Map<String, Object> result = new HashMap<>();
        result.put("logId", logId);
        result.put("question", q);
        return result;
    }

    /** 오늘 배정된 로그 항목을 반환합니다. 없으면 null. */
    public Map<String, Object> getTodayLogEntry(long userId) {
        String sql = "SELECT l.id AS log_id, q.*, w.title AS workbook_title " +
                     "FROM daily_quiz_log l " +
                     "JOIN questions q ON l.question_id = q.id " +
                     "JOIN workbooks w ON q.workbook_id = w.id " +
                     "WHERE l.user_id = ? AND l.sent_date = CURDATE()";
        String sqlOpt = "SELECT option_text FROM question_options WHERE question_id = ? ORDER BY option_order";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                QuestionDTO q = mapQuestion(rs);
                long logId = rs.getLong("log_id");

                if ("multiple".equals(q.getQuestionType())) {
                    q.setOptions(loadOptions(conn, q.getId(), sqlOpt));
                }

                Map<String, Object> result = new HashMap<>();
                result.put("logId", logId);
                result.put("question", q);
                return result;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ──────────────────────────────────────────────
    // 답변 저장 / 조회
    // ──────────────────────────────────────────────

    /**
     * 유저의 답변을 저장합니다. 이미 답변이 있으면 무시합니다(중복 제출 방지).
     *
     * @param isCorrect 자동 채점 결과 (서술형은 false로 저장 후 UI에서 별도 표시)
     */
    public void saveAnswer(long userId, long logId, String userAnswer, boolean isCorrect) {
        String sql = "INSERT IGNORE INTO daily_quiz_answers (user_id, log_id, user_answer, is_correct) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, logId);
            ps.setString(3, userAnswer);
            ps.setBoolean(4, isCorrect);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * 특정 logId에 대한 유저의 답변을 반환합니다.
     * 키: userAnswer(String), isCorrect(boolean)
     */
    public Map<String, Object> getAnswer(long userId, long logId) {
        String sql = "SELECT user_answer, is_correct, answered_at " +
                     "FROM daily_quiz_answers WHERE user_id = ? AND log_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, logId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> a = new HashMap<>();
                    a.put("userAnswer", rs.getString("user_answer"));
                    a.put("isCorrect", rs.getBoolean("is_correct"));
                    a.put("answeredAt", rs.getTimestamp("answered_at"));
                    return a;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ──────────────────────────────────────────────
    // 마이페이지 히스토리
    // ──────────────────────────────────────────────

    /**
     * 유저의 오늘의 문제 기록을 최신순으로 반환합니다.
     * 각 항목 키: date, logId, questionText, questionType, options(List),
     *              answerText, explanation, workbookTitle,
     *              userAnswer(nullable), isCorrect(boolean), answeredAt(Timestamp)
     */
    public List<Map<String, Object>> getUserHistory(long userId) {
        String sql = "SELECT l.id AS log_id, l.sent_date, " +
                     "q.id AS q_id, q.question_text, q.question_type, q.answer_text, q.explanation, " +
                     "w.title AS workbook_title, " +
                     "a.user_answer, a.is_correct, a.answered_at " +
                     "FROM daily_quiz_log l " +
                     "JOIN questions q ON l.question_id = q.id " +
                     "JOIN workbooks w ON q.workbook_id = w.id " +
                     "LEFT JOIN daily_quiz_answers a ON a.log_id = l.id AND a.user_id = l.user_id " +
                     "WHERE l.user_id = ? " +
                     "ORDER BY l.sent_date DESC";
        String sqlOpt = "SELECT option_text FROM question_options WHERE question_id = ? ORDER BY option_order";

        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date",          rs.getDate("sent_date"));
                    row.put("logId",         rs.getLong("log_id"));
                    row.put("questionText",  rs.getString("question_text"));
                    row.put("questionType",  rs.getString("question_type"));
                    row.put("answerText",    rs.getString("answer_text"));
                    row.put("explanation",   rs.getString("explanation"));
                    row.put("workbookTitle", rs.getString("workbook_title"));
                    row.put("userAnswer",    rs.getString("user_answer"));
                    row.put("isCorrect",     rs.getBoolean("is_correct"));
                    row.put("answeredAt",    rs.getTimestamp("answered_at"));

                    if ("multiple".equals(rs.getString("question_type"))) {
                        long qId = rs.getLong("q_id");
                        row.put("options", loadOptions(conn, qId, sqlOpt));
                    } else {
                        row.put("options", Collections.emptyList());
                    }
                    list.add(row);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ──────────────────────────────────────────────
    // 주제 설정
    // ──────────────────────────────────────────────

    /** 유저의 주제 설정을 반환합니다. 없으면 빈 문자열. */
    public String getUserTopic(long userId) {
        String sql = "SELECT topic FROM daily_quiz_topic WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("topic");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "";
    }

    /** 유저의 주제를 저장(없으면 INSERT, 있으면 UPDATE)합니다. */
    public void setUserTopic(long userId, String topic) {
        String sql = "INSERT INTO daily_quiz_topic (user_id, topic) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE topic = VALUES(topic), updated_at = NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, topic == null ? "" : topic.trim());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ──────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────

    private long insertLog(long userId, long questionId) {
        String sql = "INSERT IGNORE INTO daily_quiz_log (user_id, question_id, sent_date) VALUES (?, ?, CURDATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            // INSERT IGNORE로 인해 키가 없으면 이미 존재 → 오늘 항목 조회
            Map<String, Object> existing = getTodayLogEntry(userId);
            if (existing != null) return (Long) existing.get("logId");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    /**
     * 주제를 기반으로 문제를 선택합니다.
     * 최근 30일간 배정된 문제는 제외하여 반복을 줄입니다.
     * 주제와 일치하는 문제가 없으면 완전 무작위로 선택합니다.
     */
    private QuestionDTO pickQuestion(long userId, String topic) {
        String sqlOpt = "SELECT option_text FROM question_options WHERE question_id = ? ORDER BY option_order";
        String recentExclude = "(SELECT question_id FROM daily_quiz_log " +
                               " WHERE user_id = ? AND sent_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY))";

        // 주제 기반 선택
        if (topic != null && !topic.isBlank()) {
            String sql = "SELECT q.*, w.title AS workbook_title FROM questions q " +
                         "JOIN workbooks w ON q.workbook_id = w.id " +
                         "WHERE (w.subject LIKE ? OR w.title LIKE ?) " +
                         "AND q.id NOT IN " + recentExclude +
                         " ORDER BY RAND() LIMIT 1";
            QuestionDTO q = executePickQuery(sql, topic, userId, sqlOpt);
            if (q != null) return q;
        }

        // 주제 없거나 매칭 없으면 완전 무작위
        String sqlRandom = "SELECT q.*, w.title AS workbook_title FROM questions q " +
                           "JOIN workbooks w ON q.workbook_id = w.id " +
                           "WHERE q.id NOT IN " + recentExclude +
                           " ORDER BY RAND() LIMIT 1";
        QuestionDTO q = executePickQueryRandom(sqlRandom, userId, sqlOpt);
        if (q != null) return q;

        // 최근 제외 없이 완전 무작위 (DB에 문제가 매우 적을 때 fallback)
        String sqlFallback = "SELECT q.*, w.title AS workbook_title FROM questions q " +
                             "JOIN workbooks w ON q.workbook_id = w.id ORDER BY RAND() LIMIT 1";
        return executePickQueryFallback(sqlFallback, sqlOpt);
    }

    private QuestionDTO executePickQuery(String sql, String topic, long userId, String sqlOpt) {
        String like = "%" + topic + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setLong(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildQuestion(rs, conn, sqlOpt);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private QuestionDTO executePickQueryRandom(String sql, long userId, String sqlOpt) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildQuestion(rs, conn, sqlOpt);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private QuestionDTO executePickQueryFallback(String sql, String sqlOpt) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return buildQuestion(rs, conn, sqlOpt);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private QuestionDTO buildQuestion(ResultSet rs, Connection conn, String sqlOpt) throws SQLException {
        QuestionDTO q = mapQuestion(rs);
        if ("multiple".equals(q.getQuestionType())) {
            q.setOptions(loadOptions(conn, q.getId(), sqlOpt));
        }
        return q;
    }

    private QuestionDTO mapQuestion(ResultSet rs) throws SQLException {
        QuestionDTO q = new QuestionDTO();
        q.setId(rs.getLong("id"));
        q.setWorkbookId(rs.getLong("workbook_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setQuestionType(rs.getString("question_type"));
        q.setScore(rs.getInt("score"));
        q.setAnswerText(rs.getString("answer_text"));
        q.setExplanation(rs.getString("explanation"));
        q.setWorkbookTitle(rs.getString("workbook_title"));
        return q;
    }

    private List<String> loadOptions(Connection conn, long questionId, String sql) {
        List<String> opts = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) opts.add(rs.getString("option_text"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return opts;
    }
}
