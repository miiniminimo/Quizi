package com.quizi.controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.quizi.dao.WorkbookDAO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dto.QuestionDTO;

@WebServlet("/solve")
public class SolveController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 체크
        if (request.getSession().getAttribute("user") == null) {
            response.sendRedirect("views/login.jsp");
            return;
        }

        long id = Long.parseLong(request.getParameter("id"));
        WorkbookDAO dao = new WorkbookDAO();
        WorkbookDTO workbook = dao.selectById(id);
        List<QuestionDTO> questions = dao.selectQuestions(id);

        request.setAttribute("workbook", workbook);
        request.setAttribute("questions", questions);
        request.getRequestDispatcher("/views/solve.jsp").forward(request, response);
    }
}