package com.quizi.controller;

import java.io.IOException;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dao.UserDAO;
import com.quizi.dto.UserDTO;

@WebServlet("/login")
public class LoginController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO dao = new UserDAO();
        // 1. 이메일(아이디)로 사용자 정보 가져오기
        UserDTO user = dao.getUserByEmail(email);

        boolean isValid = false;

        if (user != null) {
            try {
                // 2. BCrypt 암호화된 비밀번호 검증
                if (BCrypt.checkpw(password, user.getPassword())) {
                    isValid = true;
                }
            } catch (IllegalArgumentException e) {
                // 3. [예외 처리] 평문 비밀번호일 경우 단순 비교 (admin 계정용)
                if (password.equals(user.getPassword())) {
                    isValid = true;
                }
            }
        }

        if (isValid) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            // [핵심 변경] 권한에 따른 페이지 이동 분기
            if ("ADMIN".equals(user.getRole())) {
                // 관리자면 -> 관리자 페이지로
                response.sendRedirect(request.getContextPath() + "/admin");
            } else {
                // 일반 회원이면 -> 메인 페이지로
                response.sendRedirect(request.getContextPath() + "/main");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/views/login.jsp?error=invalid");
        }
    }
}