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

@WebServlet("/detail")
public class DetailController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) { response.sendRedirect("main"); return; }

        long id = Long.parseLong(idParam);
        WorkbookDAO dao = new WorkbookDAO();
        WorkbookDTO workbook = dao.selectById(id);

        // 문항 수도 같이 보여주기 위해 문제 리스트만 살짝 가져옴 (개수 파악용)
        List<QuestionDTO> questions = dao.selectQuestions(id);

        request.setAttribute("workbook", workbook);
        request.setAttribute("questionCount", questions.size());
        request.getRequestDispatcher("/views/detail.jsp").forward(request, response);
    }
}