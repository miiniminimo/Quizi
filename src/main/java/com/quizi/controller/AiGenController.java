package com.quizi.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dto.UserDTO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dao.WorkbookDAO;
import com.quizi.service.AiService;

@WebServlet("/ai-generate")
public class AiGenController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. 로그인 체크
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/views/login.jsp");
            return;
        }

        // 파라미터 수신
        String topic = request.getParameter("topic");
        String countStr = request.getParameter("count");
        String difficulty = request.getParameter("difficulty");

        // [디테일 1] 유효성 검사 (빈 주제 방지)
        if (topic == null || topic.trim().isEmpty()) {
            // 에러 파라미터와 함께 설정 페이지로 리다이렉트
            response.sendRedirect(request.getContextPath() + "/views/ai_setup.jsp?error=empty_topic");
            return;
        }

        // [디테일 2] 숫자 파싱 예외 처리 (기본값 설정)
        int count = 5;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            // 파싱 실패시 기본값 5 유지
        }

        try {
            // 2. AI 서비스 호출
            AiService service = new AiService();
            WorkbookDTO generatedData = service.generateQuestions(topic, count, difficulty);

            // 3. DB 자동 저장 및 결과 처리
            if (generatedData != null && generatedData.getQuestions() != null && !generatedData.getQuestions().isEmpty()) {
                generatedData.setCreatorId(user.getId());

                WorkbookDAO dao = new WorkbookDAO();
                boolean success = dao.createWorkbook(generatedData);

                if (success) {
                    // [디테일 3] 성공 시 메인으로 이동하며 성공 메시지 플래그 전달
                    response.sendRedirect(request.getContextPath() + "/main?msg=ai_created");
                } else {
                    // DB 저장 실패
                    response.sendRedirect(request.getContextPath() + "/views/ai_setup.jsp?error=db_fail");
                }
            } else {
                // AI 생성 실패 (응답 없음 등)
                response.sendRedirect(request.getContextPath() + "/views/ai_setup.jsp?error=ai_fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 서버 내부 오류
            response.sendRedirect(request.getContextPath() + "/views/ai_setup.jsp?error=server_error");
        }
    }
}