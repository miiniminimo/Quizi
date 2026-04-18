package com.quizi.controller;

import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.Gson;
import com.quizi.dao.WorkbookDAO;
import com.quizi.dto.QuestionDTO;
import com.quizi.dto.UserDTO;
import com.quizi.dto.WorkbookDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/create")
public class CreateController extends HttpServlet {

    // GET 요청: 문제 만들기 페이지 보여주기
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 체크
        HttpSession session = request.getSession();
        if (session.getAttribute("user") == null) {
            response.sendRedirect("views/login.jsp");
            return;
        }
        request.getRequestDispatcher("/views/create.jsp").forward(request, response);
    }

    // POST 요청: 문제집 저장 처리 (JSON 받음)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.setStatus(401);
            return;
        }

        // 1. JSON 데이터 읽기
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        WorkbookDTO workbook = gson.fromJson(reader, WorkbookDTO.class);

        // 2. 작성자 ID 설정
        workbook.setCreatorId(user.getId());

        // 3. 정답 필수 검증
        if (workbook.getQuestions() != null) {
            for (QuestionDTO q : workbook.getQuestions()) {
                if (q.getAnswerText() == null || q.getAnswerText().trim().isEmpty()) {
                    response.setStatus(400);
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"정답이 입력되지 않은 문제가 있습니다.\"}");
                    return;
                }
            }
        }

        // 4. DB 저장
        WorkbookDAO dao = new WorkbookDAO();
        boolean success = dao.createWorkbook(workbook);

        // 5. 결과 응답
        if (success) {
            response.getWriter().write("{\"status\":\"success\"}");
        } else {
            response.setStatus(500);
            response.getWriter().write("{\"status\":\"error\"}");
        }
    }
}