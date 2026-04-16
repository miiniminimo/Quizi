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
import com.quizi.dao.SolveHistoryDAO;
import com.quizi.dto.QuestionDTO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dto.UserDTO;

@WebServlet("/grade")
public class GradeController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

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
        // [추가] DB 저장용 심플 리스트
        List<Map<String, Object>> dbDetails = new ArrayList<>();

        for (QuestionDTO q : questions) {
            totalScore += q.getScore();
            String userAnswer = request.getParameter("q-" + q.getId());
            if (userAnswer == null) userAnswer = "";

            boolean isCorrect = userAnswer.trim().equals(q.getAnswerText().trim());
            if (isCorrect) {
                earnedScore += q.getScore();
                correctCount++;
            }

            // 화면 표시용
            Map<String, Object> map = new HashMap<>();
            map.put("question", q);
            map.put("userAnswer", userAnswer);
            map.put("isCorrect", isCorrect);
            details.add(map);

            // [추가] DB 저장용 데이터 구성
            Map<String, Object> dbMap = new HashMap<>();
            dbMap.put("questionId", q.getId());
            dbMap.put("userAnswer", userAnswer);
            dbMap.put("isCorrect", isCorrect);
            dbDetails.add(dbMap);
        }

        if (user != null) {
            SolveHistoryDAO historyDao = new SolveHistoryDAO();
            // [수정] 상세 내역(dbDetails)도 함께 전달
            historyDao.saveHistory(user.getId(), workbookId, earnedScore, totalScore, correctCount, questions.size(), dbDetails);
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