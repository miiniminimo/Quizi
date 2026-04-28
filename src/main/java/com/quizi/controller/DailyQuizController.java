package com.quizi.controller;

import com.quizi.dao.DailyQuizDAO;
import com.quizi.dto.QuestionDTO;
import com.quizi.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * 오늘의 문제 관련 API 컨트롤러.
 *
 * POST /daily-quiz/answer — 답변 제출 및 채점
 * POST /daily-quiz/topic  — 주제 설정 저장
 */
@WebServlet(urlPatterns = {"/daily-quiz/answer", "/daily-quiz/topic"})
public class DailyQuizController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        UserDTO user = (session != null) ? (UserDTO) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        if ("/daily-quiz/answer".equals(path)) {
            handleAnswer(request, response, user);
        } else if ("/daily-quiz/topic".equals(path)) {
            handleTopic(request, response, user);
        }
    }

    // ─────────────────────────────────────────────
    // 답변 제출
    // ─────────────────────────────────────────────
    private void handleAnswer(HttpServletRequest req, HttpServletResponse resp, UserDTO user)
            throws IOException {

        String logIdStr    = req.getParameter("logId");
        String userAnswer  = req.getParameter("userAnswer");
        String answerText  = req.getParameter("answerText");
        String questionType = req.getParameter("questionType");

        if (logIdStr == null || userAnswer == null || userAnswer.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/main");
            return;
        }

        long logId = Long.parseLong(logIdStr);
        boolean isCorrect = grade(questionType, userAnswer.trim(), answerText);

        DailyQuizDAO dao = new DailyQuizDAO();
        dao.saveAnswer(user.getId(), logId, userAnswer.trim(), isCorrect);

        resp.sendRedirect(req.getContextPath() + "/main");
    }

    // ─────────────────────────────────────────────
    // 주제 설정
    // ─────────────────────────────────────────────
    private void handleTopic(HttpServletRequest req, HttpServletResponse resp, UserDTO user)
            throws IOException {

        String topic = req.getParameter("topic");
        DailyQuizDAO dao = new DailyQuizDAO();
        dao.setUserTopic(user.getId(), topic == null ? "" : topic.trim());

        // 저장 후 돌아갈 페이지 (referer 또는 mypage)
        String referer = req.getHeader("Referer");
        resp.sendRedirect(referer != null ? referer : req.getContextPath() + "/mypage");
    }

    // ─────────────────────────────────────────────
    // 자동 채점
    // ─────────────────────────────────────────────
    private boolean grade(String type, String userAnswer, String correctAnswer) {
        if ("essay".equals(type)) return false; // 서술형은 자동 채점 불가
        if (correctAnswer == null) return false;
        return userAnswer.equalsIgnoreCase(correctAnswer.trim());
    }
}
