package com.quizi.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dao.WorkbookDAO;
import com.quizi.dto.QuestionDTO;
import com.quizi.dto.UserDTO;
import com.quizi.dto.WorkbookDTO;

@WebServlet("/edit")
public class EditController extends HttpServlet {

    // 수정 화면 진입 (기존 데이터 불러오기)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) { response.sendRedirect("views/login.jsp"); return; }

        long id = Long.parseLong(request.getParameter("id"));
        WorkbookDAO dao = new WorkbookDAO();
        WorkbookDTO workbook = dao.selectById(id);

        // 본인 확인
        if (workbook == null || !workbook.getCreatorId().equals(user.getId())) {
            response.sendError(403, "수정 권한이 없습니다.");
            return;
        }

        // 문제 리스트 가져와서 합치기
        List<QuestionDTO> questions = dao.selectQuestions(id);
        workbook.setQuestions(questions);

        // JSON으로 변환하여 View에 전달
        Gson gson = new Gson();
        String json = gson.toJson(workbook);

        request.setAttribute("workbookJson", json); // 데이터 전달
        request.setAttribute("mode", "edit");       // 수정 모드 플래그

        request.getRequestDispatcher("/views/create.jsp").forward(request, response);
    }

    // 수정 완료 (업데이트 처리)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        WorkbookDTO workbook = gson.fromJson(reader, WorkbookDTO.class);

        WorkbookDAO dao = new WorkbookDAO();
        boolean success = dao.updateWorkbook(workbook);

        if (success) {
            response.getWriter().write("{\"status\":\"success\"}");
        } else {
            response.setStatus(500);
            response.getWriter().write("{\"status\":\"error\"}");
        }
    }
}