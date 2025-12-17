package com.quizi.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import com.google.gson.Gson;
import com.quizi.dao.WorkbookDAO;
import com.quizi.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/bookmark")
public class BookmarkController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.setStatus(401);
            return;
        }

        // JSON 데이터 읽기 ({ "workbookId": 123 })
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        Map<String, Double> data = gson.fromJson(reader, Map.class);
        long workbookId = data.get("workbookId").longValue();

        // DB 처리
        WorkbookDAO dao = new WorkbookDAO();
        boolean isSaved = dao.toggleBookmark(user.getId(), workbookId);

        // 결과 반환
        response.getWriter().write("{\"saved\": " + isSaved + "}");
    }
}