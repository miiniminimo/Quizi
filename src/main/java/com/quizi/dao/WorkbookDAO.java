package com.quizi.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.quizi.util.DBConnection;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dto.QuestionDTO;

public class WorkbookDAO {

    /**
     * 문제집 검색 / 필터.
     * - 공개(is_public=1) 문제집 또는 본인 문제집만 표시
     * - folderId 지정 시 해당 폴더 내 본인 문제집만 조회
     */
    public List<WorkbookDTO> searchWorkbooks(String keyword, String difficulty, String folderId, Long userId) {
        List<WorkbookDTO> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT w.*, u.name as creator_name FROM workbooks w JOIN users u ON w.creator_id = u.id WHERE 1=1"
        );

        // 공개/비공개 필터: 공개 문제집 또는 본인 문제집
        if (userId != null) {
            sql.append(" AND (w.is_public = 1 OR w.creator_id = ?)");
        } else {
            sql.append(" AND w.is_public = 1");
        }

        // 검색어
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (w.title LIKE ? OR w.description LIKE ? OR w.subject LIKE ?)");
        }

        // 난이도
        if (difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equals("ALL")) {
            sql.append(" AND w.difficulty = ?");
        }

        // 폴더 필터: 본인 문제집 중 해당 폴더만
        if (userId != null && folderId != null && !folderId.isEmpty()) {
            sql.append(" AND w.folder_id = ? AND w.creator_id = ?");
        }

        sql.append(" ORDER BY w.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (userId != null) {
                pstmt.setLong(paramIndex++, userId);
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword + "%";
                pstmt.setString(paramIndex++, like);
                pstmt.setString(paramIndex++, like);
                pstmt.setString(paramIndex++, like);
            }

            if (difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equals("ALL")) {
                pstmt.setString(paramIndex++, difficulty);
            }

            if (userId != null && folderId != null && !folderId.isEmpty()) {
                pstmt.setLong(paramIndex++, Long.parseLong(folderId));
                pstmt.setLong(paramIndex++, userId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapRowToWorkbook(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<WorkbookDTO> selectAll() {
        return searchWorkbooks(null, null, null, null);
    }

    public boolean createWorkbook(WorkbookDTO workbook) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean result = false;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlWb = "INSERT INTO workbooks (creator_id, title, description, subject, difficulty, time_limit, is_public) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sqlWb, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, workbook.getCreatorId());
            pstmt.setString(2, workbook.getTitle());
            pstmt.setString(3, workbook.getDescription());
            pstmt.setString(4, workbook.getSubject());
            pstmt.setString(5, workbook.getDifficulty());
            pstmt.setInt(6, workbook.getTimeLimit());
            pstmt.setBoolean(7, workbook.isPublic());
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            long workbookId = 0;
            if (rs.next()) workbookId = rs.getLong(1);
            else throw new SQLException("Creating workbook failed, no ID obtained.");

            insertQuestions(conn, workbookId, workbook.getQuestions());

            conn.commit();
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            DBConnection.close(conn);
        }
        return result;
    }

    public boolean updateWorkbook(WorkbookDTO workbook) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean result = false;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlWb = "UPDATE workbooks SET title=?, description=?, subject=?, difficulty=?, time_limit=?, is_public=? WHERE id=?";
            pstmt = conn.prepareStatement(sqlWb);
            pstmt.setString(1, workbook.getTitle());
            pstmt.setString(2, workbook.getDescription());
            pstmt.setString(3, workbook.getSubject());
            pstmt.setString(4, workbook.getDifficulty());
            pstmt.setInt(5, workbook.getTimeLimit());
            pstmt.setBoolean(6, workbook.isPublic());
            pstmt.setLong(7, workbook.getId());
            int affected = pstmt.executeUpdate();

            if(affected == 0) throw new SQLException("Update failed");

            String sqlDel = "DELETE FROM questions WHERE workbook_id=?";
            pstmt = conn.prepareStatement(sqlDel);
            pstmt.setLong(1, workbook.getId());
            pstmt.executeUpdate();

            insertQuestions(conn, workbook.getId(), workbook.getQuestions());

            conn.commit();
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
        } finally {
            DBConnection.close(conn);
        }
        return result;
    }

    public WorkbookDTO selectById(long id) {
        String sql = "SELECT w.*, u.name as creator_name FROM workbooks w JOIN users u ON w.creator_id = u.id WHERE w.id = ?";
        WorkbookDTO dto = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    dto = mapRowToWorkbook(rs);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return dto;
    }

    /**
     * 문제집의 모든 문제 + 보기를 단일 LEFT JOIN 쿼리로 조회합니다. (N+1 방지)
     */
    public List<QuestionDTO> selectQuestions(long workbookId) {
        List<QuestionDTO> list = new ArrayList<>();
        // LEFT JOIN으로 문제와 보기를 한 번에 가져옴 (객관식 외 문제는 opt 컬럼이 null)
        String sql = "SELECT q.*, o.option_text, o.option_order " +
                     "FROM questions q " +
                     "LEFT JOIN question_options o ON o.question_id = q.id " +
                     "WHERE q.workbook_id = ? " +
                     "ORDER BY q.id ASC, o.option_order ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, workbookId);
            try (ResultSet rs = ps.executeQuery()) {
                QuestionDTO current = null;
                while (rs.next()) {
                    long qId = rs.getLong("id");
                    // 새 문제 시작
                    if (current == null || current.getId() != qId) {
                        current = new QuestionDTO();
                        current.setId(qId);
                        current.setWorkbookId(rs.getLong("workbook_id"));
                        current.setQuestionText(rs.getString("question_text"));
                        current.setQuestionType(rs.getString("question_type"));
                        current.setScore(rs.getInt("score"));
                        current.setAnswerText(rs.getString("answer_text"));
                        current.setExplanation(rs.getString("explanation"));
                        if ("multiple".equals(current.getQuestionType())) {
                            current.setOptions(new ArrayList<>());
                        }
                        list.add(current);
                    }
                    // 보기 추가
                    String optText = rs.getString("option_text");
                    if (optText != null && current.getOptions() != null) {
                        current.getOptions().add(optText);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<WorkbookDTO> selectByCreator(long creatorId) {
        List<WorkbookDTO> list = new ArrayList<>();
        String sql = "SELECT w.*, (SELECT name FROM users WHERE id=w.creator_id) as creator_name FROM workbooks w WHERE creator_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, creatorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToWorkbook(rs));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * 북마크 토글. 동시 요청에도 안전한 단일 트랜잭션으로 처리합니다.
     * @return true = 북마크 추가됨, false = 북마크 해제됨
     */
    public boolean toggleBookmark(long userId, long workbookId) {
        // INSERT IGNORE: 이미 있으면 0행 영향 → 삭제 분기
        String insertSql = "INSERT IGNORE INTO bookmarks (user_id, workbook_id) VALUES (?, ?)";
        String deleteSql = "DELETE FROM bookmarks WHERE user_id=? AND workbook_id=?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int inserted;
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setLong(1, userId);
                    ps.setLong(2, workbookId);
                    inserted = ps.executeUpdate();
                }
                if (inserted > 0) {
                    conn.commit();
                    return true; // 새로 저장됨
                }
                // 이미 존재 → 삭제
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setLong(1, userId);
                    ps.setLong(2, workbookId);
                    ps.executeUpdate();
                }
                conn.commit();
                return false; // 해제됨
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<Long> selectSavedWorkbookIds(long userId) {
        List<Long> list = new ArrayList<>();
        String sql = "SELECT workbook_id FROM bookmarks WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(rs.getLong("workbook_id"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<WorkbookDTO> selectSavedWorkbooks(long userId) {
        List<WorkbookDTO> list = new ArrayList<>();
        String sql = "SELECT w.*, u.name as creator_name FROM bookmarks b JOIN workbooks w ON b.workbook_id = w.id JOIN users u ON w.creator_id = u.id WHERE b.user_id = ? ORDER BY b.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToWorkbook(rs));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * DB에서 무작위 문제 하나를 뽑아 반환합니다. (일일 슬랙 알림용)
     *
     * ORDER BY RAND() 대신 COUNT + OFFSET 방식으로 성능 개선:
     * - ORDER BY RAND()는 전체 테이블을 정렬(O(n log n))
     * - LIMIT 1 OFFSET random은 인덱스 스캔(O(n))으로 대규모 데이터에서 훨씬 빠름
     */
    public QuestionDTO selectRandomQuestion() {
        String sqlCount = "SELECT COUNT(*) FROM questions q JOIN workbooks w ON q.workbook_id = w.id";
        String sqlSelect = "SELECT q.*, w.title AS workbook_title FROM questions q " +
                           "JOIN workbooks w ON q.workbook_id = w.id " +
                           "LIMIT 1 OFFSET ?";
        String sqlOpt = "SELECT option_text FROM question_options WHERE question_id = ? ORDER BY option_order ASC";

        try (Connection conn = DBConnection.getConnection()) {
            // 1. 총 문제 수 조회
            int total;
            try (PreparedStatement ps = conn.prepareStatement(sqlCount);
                 ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) == 0) return null;
                total = rs.getInt(1);
            }

            // 2. 무작위 offset 선택 후 단일 조회
            int offset = ThreadLocalRandom.current().nextInt(total);
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
                ps.setInt(1, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;

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
                        try (PreparedStatement psOpt = conn.prepareStatement(sqlOpt)) {
                            psOpt.setLong(1, q.getId());
                            try (ResultSet rsOpt = psOpt.executeQuery()) {
                                while (rsOpt.next()) options.add(rsOpt.getString("option_text"));
                            }
                        }
                        q.setOptions(options);
                    }
                    return q;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean deleteWorkbook(long workbookId) {
        String sql = "DELETE FROM workbooks WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, workbookId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private WorkbookDTO mapRowToWorkbook(ResultSet rs) throws SQLException {
        WorkbookDTO dto = new WorkbookDTO();
        dto.setId(rs.getLong("id"));
        dto.setCreatorId(rs.getLong("creator_id"));
        dto.setTitle(rs.getString("title"));
        dto.setDescription(rs.getString("description"));
        dto.setSubject(rs.getString("subject"));
        dto.setDifficulty(rs.getString("difficulty"));
        dto.setTimeLimit(rs.getInt("time_limit"));
        dto.setLikesCount(rs.getInt("likes_count"));
        dto.setPlaysCount(rs.getInt("plays_count"));
        dto.setPublic(rs.getBoolean("is_public"));
        dto.setCreatorName(rs.getString("creator_name"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        return dto;
    }

    private void insertQuestions(Connection conn, long workbookId, List<QuestionDTO> questions) throws SQLException {
        String sqlQ = "INSERT INTO questions (workbook_id, question_text, question_type, score, answer_text, explanation) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlOpt = "INSERT INTO question_options (question_id, option_text, option_order) VALUES (?, ?, ?)";

        try (PreparedStatement pstmtQ = conn.prepareStatement(sqlQ, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmtOpt = conn.prepareStatement(sqlOpt)) {

            for (QuestionDTO q : questions) {
                pstmtQ.setLong(1, workbookId);
                pstmtQ.setString(2, q.getQuestionText());
                pstmtQ.setString(3, q.getQuestionType());
                pstmtQ.setInt(4, q.getScore());
                pstmtQ.setString(5, q.getAnswerText());
                pstmtQ.setString(6, q.getExplanation());
                pstmtQ.executeUpdate();

                ResultSet rsQ = pstmtQ.getGeneratedKeys();
                long questionId = 0;
                if (rsQ.next()) questionId = rsQ.getLong(1);
                rsQ.close();

                if ("multiple".equals(q.getQuestionType()) && q.getOptions() != null) {
                    int order = 1;
                    for (String optText : q.getOptions()) {
                        pstmtOpt.setLong(1, questionId);
                        pstmtOpt.setString(2, optText);
                        pstmtOpt.setInt(3, order++);
                        pstmtOpt.addBatch();
                    }
                    pstmtOpt.executeBatch();
                }
            }
        }
    }
}