package com.quizi.controller;

import java.io.IOException;
import com.quizi.dao.WorkbookDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/delete")
public class DeleteController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 본인 확인 로직이 필요하지만, 여기선 간단히 구현
        long workbookId = Long.parseLong(request.getParameter("id"));

        WorkbookDAO dao = new WorkbookDAO();
        boolean success = dao.deleteWorkbook(workbookId);

        if (success) {
            response.setStatus(200);
        } else {
            response.sendError(500);
        }
    }
}