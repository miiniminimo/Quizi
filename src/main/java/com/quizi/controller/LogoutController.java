package com.quizi.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 삭제 (로그아웃)
        }

        // [수정] 로그아웃 후 로그인 페이지(/login)로 리다이렉트
        // LoginController의 doGet이 실행되어 views/login.jsp를 보여줍니다.
        response.sendRedirect(request.getContextPath() + "/login");
    }
}