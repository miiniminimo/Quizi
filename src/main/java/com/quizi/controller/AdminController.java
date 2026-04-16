package com.quizi.controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dto.UserDTO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dao.UserDAO;
import com.quizi.dao.WorkbookDAO;

@WebServlet("/admin")
public class AdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main");
            return;
        }

        UserDAO userDAO = new UserDAO();
        WorkbookDAO workbookDAO = new WorkbookDAO();

        List<UserDTO> allUsers = userDAO.getAllUsers();
        List<WorkbookDTO> allWorkbooks = workbookDAO.selectAll();

        request.setAttribute("userCount", allUsers.size());
        request.setAttribute("workbookCount", allWorkbooks.size());
        request.setAttribute("allUsers", allUsers);
        request.setAttribute("allWorkbooks", allWorkbooks);

        request.getRequestDispatcher("/views/admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendError(403, "권한이 없습니다.");
            return;
        }

        String action = request.getParameter("action");

        // [추가됨] 문제집 일괄 삭제 로직
        if ("deleteBulkWorkbooks".equals(action)) {
            String idsParam = request.getParameter("ids"); // "1,2,5" 형태
            if (idsParam != null && !idsParam.isEmpty()) {
                String[] ids = idsParam.split(",");
                WorkbookDAO dao = new WorkbookDAO();
                boolean allSuccess = true;

                for (String idStr : ids) {
                    try {
                        long id = Long.parseLong(idStr);
                        if (!dao.deleteWorkbook(id)) allSuccess = false;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                if (allSuccess) response.setStatus(200);
                else response.sendError(500, "일부 항목 삭제 실패");
            } else {
                response.sendError(400, "삭제할 항목이 없습니다.");
            }
            return;
        }

        // 기존 단일 삭제 로직
        String idStr = request.getParameter("id");
        if (idStr != null) {
            long id = Long.parseLong(idStr);
            boolean success = false;

            if ("deleteUser".equals(action)) {
                UserDAO dao = new UserDAO();
                if (id == user.getId()) {
                    response.sendError(400, "자기 자신은 삭제할 수 없습니다.");
                    return;
                }
                success = dao.deleteUser(id);
            } else if ("deleteWorkbook".equals(action)) {
                WorkbookDAO dao = new WorkbookDAO();
                success = dao.deleteWorkbook(id);
            }

            if(success) response.setStatus(200);
            else response.sendError(500);
        }
    }
}
