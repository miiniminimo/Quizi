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
        UserDTO user = dao.getUserByEmail(email);

        boolean isValid = false;

        if (user != null && user.getPassword() != null) {
            try {
                isValid = BCrypt.checkpw(password, user.getPassword());
            } catch (IllegalArgumentException e) {
                // BCrypt 형식이 아닌 비밀번호는 인증 실패로 처리
                isValid = false;
            }
        }

        if (isValid) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

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