package com.quizi.controller;

import java.io.IOException;
import com.quizi.dao.WorkbookDAO;
import com.quizi.dto.UserDTO;
import com.quizi.dto.WorkbookDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/delete")
public class DeleteController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // ── 로그인 확인
        HttpSession session = request.getSession(false);
        UserDTO user = (session != null) ? (UserDTO) session.getAttribute("user") : null;
        if (user == null) {
            response.sendError(401, "로그인이 필요합니다.");
            return;
        }

        long workbookId = Long.parseLong(request.getParameter("id"));
        WorkbookDAO dao = new WorkbookDAO();

        // ── 소유권 확인 (ADMIN은 모든 문제집 삭제 가능)
        if (!"ADMIN".equals(user.getRole())) {
            WorkbookDTO workbook = dao.selectById(workbookId);
            if (workbook == null || !workbook.getCreatorId().equals(user.getId())) {
                response.sendError(403, "삭제 권한이 없습니다.");
                return;
            }
        }

        boolean success = dao.deleteWorkbook(workbookId);
        if (success) {
            response.setStatus(200);
        } else {
            response.sendError(500);
        }
    }
}