package com.quizi.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dao.WorkbookDAO;
import com.quizi.dao.SolveHistoryDAO; // 추가
import com.quizi.dto.QuestionDTO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dto.UserDTO;

@WebServlet("/grade")
public class GradeController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 로그인 체크 (비로그인 시 저장 안 함 or 로그인 페이지로)
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        long workbookId = Long.parseLong(request.getParameter("workbookId"));
        WorkbookDAO dao = new WorkbookDAO();

        WorkbookDTO workbook = dao.selectById(workbookId);
        List<QuestionDTO> questions = dao.selectQuestions(workbookId);

        int totalScore = 0;
        int earnedScore = 0;
        int correctCount = 0;

        List<Map<String, Object>> details = new ArrayList<>();

        for (QuestionDTO q : questions) {
            totalScore += q.getScore();
            String userAnswer = request.getParameter("q-" + q.getId());
            if (userAnswer == null) userAnswer = "";

            boolean isCorrect = userAnswer.trim().equals(q.getAnswerText().trim());
            if (isCorrect) {
                earnedScore += q.getScore();
                correctCount++;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("question", q);
            map.put("userAnswer", userAnswer);
            map.put("isCorrect", isCorrect);
            details.add(map);
        }

        // [추가된 부분] 학습 기록 DB 저장
        if (user != null) {
            SolveHistoryDAO historyDao = new SolveHistoryDAO();
            historyDao.saveHistory(user.getId(), workbookId, earnedScore, totalScore, correctCount, questions.size());

            // 문제집 풀이 횟수 증가 (선택사항 - WorkbookDAO에 메서드 필요)
            // dao.incrementPlayCount(workbookId);
        }

        request.setAttribute("workbook", workbook);
        request.setAttribute("totalScore", totalScore);
        request.setAttribute("earnedScore", earnedScore);
        request.setAttribute("correctCount", correctCount);
        request.setAttribute("totalCount", questions.size());
        request.setAttribute("details", details);

        request.getRequestDispatcher("/views/result.jsp").forward(request, response);
    }
}