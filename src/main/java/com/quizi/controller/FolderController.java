package com.quizi.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.quizi.dao.FolderDAO;
import com.quizi.dto.UserDTO;

@WebServlet("/folder/*")
public class FolderController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); // /create, /move, /delete 등이 들어옴
        UserDTO user = (UserDTO) request.getSession().getAttribute("user");

        if (user == null) {
            response.sendError(401);
            return;
        }

        FolderDAO dao = new FolderDAO();
        boolean success = false;

        if ("/create".equals(pathInfo)) {
            String name = request.getParameter("name");
            success = dao.createFolder(user.getId(), name);
        } else if ("/move".equals(pathInfo)) {
            long workbookId = Long.parseLong(request.getParameter("workbookId"));
            String folderId = request.getParameter("folderId"); // 빈 문자열이면 루트로 이동
            success = dao.moveWorkbook(workbookId, folderId);
        } else if ("/delete".equals(pathInfo)) {
            long id = Long.parseLong(request.getParameter("id"));
            success = dao.deleteFolder(id);
        }

        if (success) {
            response.setStatus(200);
        } else {
            response.sendError(500);
        }
    }
}