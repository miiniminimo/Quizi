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

    // 관리자 페이지 조회
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        // [보안] 관리자가 아니면 메인으로 리다이렉트
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main");
            return;
        }

        UserDAO userDAO = new UserDAO();
        WorkbookDAO workbookDAO = new WorkbookDAO();

        // 전체 회원 및 문제집 목록 가져오기
        List<UserDTO> allUsers = userDAO.getAllUsers();
        List<WorkbookDTO> allWorkbooks = workbookDAO.selectAll();

        request.setAttribute("userCount", allUsers.size());
        request.setAttribute("workbookCount", allWorkbooks.size());
        request.setAttribute("allUsers", allUsers);
        request.setAttribute("allWorkbooks", allWorkbooks);

        request.getRequestDispatcher("/views/admin.jsp").forward(request, response);
    }

    // 삭제 요청 처리 (AJAX)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        // [보안] 관리자 권한 체크
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendError(403, "권한이 없습니다.");
            return;
        }

        String action = request.getParameter("action");
        String idStr = request.getParameter("id");

        if (idStr != null) {
            long id = Long.parseLong(idStr);
            boolean success = false;

            if ("deleteUser".equals(action)) {
                UserDAO dao = new UserDAO();
                // 자기 자신 삭제 방지
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