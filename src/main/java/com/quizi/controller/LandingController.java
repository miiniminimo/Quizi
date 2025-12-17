package com.quizi.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dto.UserDTO;

@WebServlet("/welcome")
public class LandingController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 이미 로그인한 상태라면 바로 메인으로 이동
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user != null) {
            response.sendRedirect(request.getContextPath() + "/main");
            return;
        }

        // 아니면 랜딩 페이지 보여줌
        request.getRequestDispatcher("/views/landing.jsp").forward(request, response);
    }
}