package com.quizi.dao;

import com.quizi.dto.QuestionDTO;
import com.quizi.util.ConfigManager;
import com.quizi.util.DBConnection;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 유저별 퀴즈 문제 배정을 담당하는 DAO.
 *
 * 테이블 의존:
 *   daily_quiz_log     — 유저별 문제 배정 이력 (시간 기반, 주기마다 갱신)
 *   daily_quiz_answers — 유저의 답변 기록
 *   daily_quiz_topic   — 유저의 주제 설정
 *
 * quiz.interval.minutes (config.properties) 값에 따라
 * Slack 전송 주기와 웹 문제 갱신 주기가 함께 결정됩니다.
 */
public class DailyQuizDAO {

    private int getIntervalMinutes() {
        String val = ConfigManager.getProperty("quiz.interval.minutes");
        if (val == null || val.isBlank()) return 10;
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return 10; }
    }

    // ──────────────────────────────────────────────
    // 문제 조회 / 생성 (시간 기반 lazy)
    // ──────────────────────────────────────────────

    /**
     * 최근 N분 이내에 배정된 문제가 있으면 반환하고,
     * 없으면 주제 기반으로 새 문제를 선택해 로그에 기록합니다.
     * (웹 페이지용 — 같은 주기 내에서는 동일 문제 유지)
     *
     * @return {logId, question(QuestionDTO)} 또는 빈 Map (DB에 문제 없음)
     */
    public Map<String, Object> getOrCreateEntry(long userId) {
        Map<String, Object> existing = getLatestEntry(userId);
        if (existing != null) return existing;

        return forceNewEntry(userId);
    }

    /**
     * 무조건 새 문제를 배정합니다.
     * (스케줄러용 — 매 주기마다 반드시 새 문제 생성)
     *
     * @return {logId, question(QuestionDTO)} 또는 빈 Map (DB에 문제 없음)
     */
    public Map<String, Object> forceNewEntry(long userId) {
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

    /**
     * 최근 N분 이내 배정된 로그 항목을 반환합니다. 없으면 null.
     * (N = quiz.interval.minutes in config.properties)
     */
    public Map<String, Object> getLatestEntry(long userId) {
        int interval = getIntervalMinutes();
        String sql = "SELECT l.id AS log_id, q.*, w.title AS workbook_title " +
                     "FROM daily_quiz_log l " +
                     "JOIN questions q ON l.question_id = q.id " +
                     "JOIN workbooks w ON q.workbook_id = w.id " +
                     "WHERE l.user_id = ? AND l.created_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE) " +
                     "ORDER BY l.created_at DESC LIMIT 1";
        String sqlOpt = "SELECT option_text FROM question_options WHERE question_id = ? ORDER BY option_order";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, interval);
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
    // 주제 설정 유저 목록
    // ──────────────────────────────────────────────

    /** daily_quiz_topic에 주제를 등록한 모든 유저 ID를 반환합니다. */
    public List<Long> getAllTopicUserIds() {
        List<Long> ids = new ArrayList<>();
        String sql = "SELECT user_id FROM daily_quiz_topic WHERE topic IS NOT NULL AND topic != ''";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getLong("user_id"));
        } catch (Exception e) { e.printStackTrace(); }
        return ids;
    }

    // ──────────────────────────────────────────────
    // Slack 전송용 주제 기반 랜덤 선택
    // ──────────────────────────────────────────────

    /**
     * Slack 알림용 문제를 선택합니다.
     * daily_quiz_topic 테이블에 등록된 주제들 중 하나를 무작위로 골라,
     * 해당 주제와 연관된 문제를 반환합니다.
     * 주제가 하나도 없으면 전체 문제 중 무작위 선택.
     */
    public QuestionDTO pickQuestionForSlack() {
        // 1. 등록된 모든 주제 수집
        List<String> topics = new ArrayList<>();
        String sqlTopics = "SELECT DISTINCT topic FROM daily_quiz_topic WHERE topic IS NOT NULL AND topic != ''";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlTopics);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) topics.add(rs.getString("topic"));
        } catch (Exception e) { e.printStackTrace(); }

        // 2. 주제 중 하나를 랜덤 선택 (없으면 null → 전체 랜덤)
        String topic = topics.isEmpty() ? null
                : topics.get(ThreadLocalRandom.current().nextInt(topics.size()));

        // 3. 주제 기반으로 문제 선택 (userId=null → 최근 제외 없이)
        String sqlOpt = "SELECT option_text FROM question_options WHERE question_id = ? ORDER BY option_order";

        if (topic != null) {
            String like = "%" + topic + "%";
            String sqlIds = "SELECT q.id FROM questions q JOIN workbooks w ON q.workbook_id = w.id " +
                            "WHERE (w.subject LIKE ? OR w.title LIKE ?)";
            Long qId = pickRandomId(sqlIds, like, like, null);
            if (qId != null) {
                System.out.printf("[QuizScheduler] 주제 '%s' 기반 문제 선택 (id=%d)%n", topic, qId);
                return fetchById(qId, sqlOpt);
            }
        }

        // 4. Fallback: 전체 문제 중 랜덤
        String sqlAll = "SELECT q.id FROM questions q JOIN workbooks w ON q.workbook_id = w.id";
        Long qId = pickRandomId(sqlAll, null, null, null);
        if (qId != null) return fetchById(qId, sqlOpt);

        return null;
    }

    // ──────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────

    private long insertLog(long userId, long questionId) {
        String sql = "INSERT INTO daily_quiz_log (user_id, question_id, sent_date, created_at) " +
                     "VALUES (?, ?, CURDATE(), NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    /**
     * 주제를 기반으로 문제를 선택합니다.
     * 최근 30일간 배정된 문제는 제외하여 반복을 줄입니다.
     * 주제와 일치하는 문제가 없으면 완전 무작위로 선택합니다.
     *
     * ORDER BY RAND() 대신 후보 ID 목록에서 무작위 선택하는 방식으로 성능 개선:
     * - 조건에 맞는 question_id만 먼저 가져온 뒤 Java에서 random 선택
     * - 대규모 데이터에서 전체 테이블 정렬(ORDER BY RAND) 회피
     */
    private QuestionDTO pickQuestion(long userId, String topic) {
        String sqlOpt = "SELECT option_text FROM question_options WHERE question_id = ? ORDER BY option_order";
        String recentExcludeClause = "q.id NOT IN (SELECT question_id FROM daily_quiz_log " +
                                     "WHERE user_id = ? AND sent_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY))";

        // ── 1. 주제 기반 후보 ID 조회
        if (topic != null && !topic.isBlank()) {
            String like = "%" + topic + "%";
            String sqlIds = "SELECT q.id FROM questions q JOIN workbooks w ON q.workbook_id = w.id " +
                            "WHERE (w.subject LIKE ? OR w.title LIKE ?) AND " + recentExcludeClause;
            Long qId = pickRandomId(sqlIds, like, like, userId);
            if (qId != null) return fetchById(qId, sqlOpt);
        }

        // ── 2. 주제 무관 후보 ID 조회 (최근 제외)
        String sqlIds2 = "SELECT q.id FROM questions q JOIN workbooks w ON q.workbook_id = w.id " +
                         "WHERE " + recentExcludeClause;
        Long qId2 = pickRandomId(sqlIds2, null, null, userId);
        if (qId2 != null) return fetchById(qId2, sqlOpt);

        // ── 3. Fallback: 제한 없이 전체에서 선택 (DB에 문제가 매우 적을 때)
        String sqlAll = "SELECT q.id FROM questions q JOIN workbooks w ON q.workbook_id = w.id";
        Long qId3 = pickRandomId(sqlAll, null, null, null);
        if (qId3 != null) return fetchById(qId3, sqlOpt);

        return null;
    }

    /**
     * 후보 ID 목록을 가져온 뒤 Java ThreadLocalRandom으로 하나를 선택합니다.
     * params: [like1, like2 (nullable), userId (nullable)]
     */
    private Long pickRandomId(String sql, String like1, String like2, Long userId) {
        List<Long> ids = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (like1 != null) { ps.setString(idx++, like1); ps.setString(idx++, like2); }
            if (userId != null) ps.setLong(idx, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong(1));
            }
        } catch (Exception e) { e.printStackTrace(); }
        if (ids.isEmpty()) return null;
        return ids.get(ThreadLocalRandom.current().nextInt(ids.size()));
    }

    /** 문제 ID로 단건 조회 */
    private QuestionDTO fetchById(long questionId, String sqlOpt) {
        String sql = "SELECT q.*, w.title AS workbook_title FROM questions q " +
                     "JOIN workbooks w ON q.workbook_id = w.id WHERE q.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildQuestion(rs, conn, sqlOpt);
            }
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
