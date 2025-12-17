package com.quizi.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.quizi.util.DBConnection;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dto.QuestionDTO;

public class WorkbookDAO {

    // 1. 모든 문제집 목록 조회 (메인 페이지용 - 최신순)
    public List<WorkbookDTO> selectAll() {
        List<WorkbookDTO> list = new ArrayList<>();
        String sql = "SELECT w.*, u.name as creator_name FROM workbooks w JOIN users u ON w.creator_id = u.id ORDER BY w.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                WorkbookDTO dto = mapRowToWorkbook(rs);
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. 검색 및 필터링 (동적 쿼리)
    public List<WorkbookDTO> searchWorkbooks(String keyword, String difficulty) {
        List<WorkbookDTO> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT w.*, u.name as creator_name FROM workbooks w JOIN users u ON w.creator_id = u.id WHERE 1=1"
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (w.title LIKE ? OR w.description LIKE ? OR w.subject LIKE ?)");
        }

        if (difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equals("ALL")) {
            sql.append(" AND w.difficulty = ?");
        }

        sql.append(" ORDER BY w.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword + "%";
                pstmt.setString(paramIndex++, likePattern);
                pstmt.setString(paramIndex++, likePattern);
                pstmt.setString(paramIndex++, likePattern);
            }

            if (difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equals("ALL")) {
                pstmt.setString(paramIndex++, difficulty);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WorkbookDTO dto = mapRowToWorkbook(rs);
                    list.add(dto);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 3. 문제집 생성 (트랜잭션 처리)
    public boolean createWorkbook(WorkbookDTO workbook) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean result = false;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. 문제집 정보 저장
            String sqlWb = "INSERT INTO workbooks (creator_id, title, description, subject, difficulty, time_limit) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sqlWb, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, workbook.getCreatorId());
            pstmt.setString(2, workbook.getTitle());
            pstmt.setString(3, workbook.getDescription());
            pstmt.setString(4, workbook.getSubject());
            pstmt.setString(5, workbook.getDifficulty());
            pstmt.setInt(6, workbook.getTimeLimit());
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            long workbookId = 0;
            if (rs.next()) workbookId = rs.getLong(1);
            else throw new SQLException("Creating workbook failed, no ID obtained.");

            // 2. 문제 및 보기 저장
            insertQuestions(conn, workbookId, workbook.getQuestions());

            conn.commit();
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            DBConnection.close(conn);
        }
        return result;
    }

    // 4. 문제집 수정 (트랜잭션: 정보 업데이트 + 기존 문제 삭제 후 재등록)
    public boolean updateWorkbook(WorkbookDTO workbook) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean result = false;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. 기본 정보 업데이트
            String sqlWb = "UPDATE workbooks SET title=?, description=?, subject=?, difficulty=?, time_limit=? WHERE id=?";
            pstmt = conn.prepareStatement(sqlWb);
            pstmt.setString(1, workbook.getTitle());
            pstmt.setString(2, workbook.getDescription());
            pstmt.setString(3, workbook.getSubject());
            pstmt.setString(4, workbook.getDifficulty());
            pstmt.setInt(5, workbook.getTimeLimit());
            pstmt.setLong(6, workbook.getId());
            int affected = pstmt.executeUpdate();

            if(affected == 0) throw new SQLException("Update failed");

            // 2. 기존 문제 삭제
            String sqlDel = "DELETE FROM questions WHERE workbook_id=?";
            pstmt = conn.prepareStatement(sqlDel);
            pstmt.setLong(1, workbook.getId());
            pstmt.executeUpdate();

            // 3. 새 문제 입력
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

    // 5. ID로 문제집 상세 정보 조회
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

    // 6. 특정 문제집의 문제 목록 조회 (보기 포함)
    public List<QuestionDTO> selectQuestions(long workbookId) {
        List<QuestionDTO> list = new ArrayList<>();
        String sqlQ = "SELECT * FROM questions WHERE workbook_id = ? ORDER BY id ASC";
        String sqlOpt = "SELECT option_text FROM question_options WHERE question_id = ? ORDER BY option_order ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmtQ = conn.prepareStatement(sqlQ)) {

            pstmtQ.setLong(1, workbookId);
            try (ResultSet rsQ = pstmtQ.executeQuery()) {
                while (rsQ.next()) {
                    QuestionDTO q = new QuestionDTO();
                    q.setId(rsQ.getLong("id"));
                    q.setWorkbookId(rsQ.getLong("workbook_id"));
                    q.setQuestionText(rsQ.getString("question_text"));
                    q.setQuestionType(rsQ.getString("question_type"));
                    q.setScore(rsQ.getInt("score"));
                    q.setAnswerText(rsQ.getString("answer_text"));
                    q.setExplanation(rsQ.getString("explanation"));

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
                    list.add(q);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 7. 특정 사용자가 만든 문제집 목록 조회
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

    // 8. 북마크 토글 (있으면 삭제, 없으면 추가)
    public boolean toggleBookmark(long userId, long workbookId) {
        String checkSql = "SELECT 1 FROM bookmarks WHERE user_id=? AND workbook_id=?";
        boolean exists = false;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, workbookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) exists = true;
            }

            String actionSql = exists ? "DELETE FROM bookmarks WHERE user_id=? AND workbook_id=?"
                    : "INSERT INTO bookmarks (user_id, workbook_id) VALUES (?, ?)";

            try (PreparedStatement actionPstmt = conn.prepareStatement(actionSql)) {
                actionPstmt.setLong(1, userId);
                actionPstmt.setLong(2, workbookId);
                actionPstmt.executeUpdate();
            }
            return !exists;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 9. 사용자가 북마크한 문제집 ID 목록 조회
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

    // 10. 사용자가 저장한 문제집 전체 목록 조회
    public List<WorkbookDTO> selectSavedWorkbooks(long userId) {
        List<WorkbookDTO> list = new ArrayList<>();
        String sql = "SELECT w.*, u.name as creator_name FROM bookmarks b " +
                "JOIN workbooks w ON b.workbook_id = w.id " +
                "JOIN users u ON w.creator_id = u.id " +
                "WHERE b.user_id = ? ORDER BY b.created_at DESC";
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

    // 11. 문제집 삭제
    public boolean deleteWorkbook(long workbookId) {
        String sql = "DELETE FROM workbooks WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, workbookId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // --- Helper Methods ---

    // ResultSet에서 WorkbookDTO 매핑
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
        dto.setCreatorName(rs.getString("creator_name"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        return dto;
    }

    // 문제 리스트 일괄 Insert (생성/수정 공통 사용)
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