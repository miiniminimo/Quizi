package com.quizi.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dao.WrongNoteDAO;
import com.quizi.dto.UserDTO;

@WebServlet("/wrongnote/save")
public class WrongNoteController extends HttpServlet {
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

        // JSON 데이터 파싱 (List<Map> 형태)
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        List<Map<String, Object>> notes = gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>(){}.getType());

        // DB 저장
        WrongNoteDAO dao = new WrongNoteDAO();
        int savedCount = dao.saveWrongNotes(user.getId(), notes);

        // 결과 반환
        response.getWriter().write("{\"count\": " + savedCount + "}");
    }
}