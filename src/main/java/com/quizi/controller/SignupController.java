package com.quizi.controller;

import java.io.IOException;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.quizi.dao.UserDAO;
import com.quizi.dto.UserDTO;

@WebServlet("/signup")
public class SignupController extends HttpServlet {

    // [추가됨] GET 요청 시 회원가입 화면으로 포워딩
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/views/signup.jsp").forward(request, response);
    }

    // POST 요청 시 회원가입 처리 (기존 로직 유지)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 비밀번호 암호화
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        UserDTO newUser = new UserDTO();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);

        UserDAO dao = new UserDAO();
        int result = dao.signUp(newUser);

        if (result > 0) {
            // 회원가입 성공 시 로그인 페이지로 이동 (메시지 포함)
            response.sendRedirect(request.getContextPath() + "/login?msg=signup_success");
        } else {
            // 실패 시 다시 가입 페이지로
            response.sendRedirect(request.getContextPath() + "/signup?error=fail");
        }
    }
}