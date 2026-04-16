package com.quizi.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dao.SolveHistoryDAO;
import com.quizi.dto.UserDTO;

@WebServlet("/history/detail")
public class HistoryDetailController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/views/login.jsp");
            return;
        }

        long historyId = Long.parseLong(request.getParameter("id"));
        String workbookTitle = request.getParameter("title"); // 목록에서 제목을 받아옴 (편의상)

        SolveHistoryDAO dao = new SolveHistoryDAO();
        List<Map<String, Object>> details = dao.selectHistoryDetails(historyId);

        request.setAttribute("historyDetails", details);
        request.setAttribute("workbookTitle", workbookTitle);

        request.getRequestDispatcher("/views/history_detail.jsp").forward(request, response);
    }
}