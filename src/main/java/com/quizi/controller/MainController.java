package com.quizi.controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dao.WorkbookDAO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dto.UserDTO;

@WebServlet("/main")
public class MainController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        // 검색 파라미터 받기
        String keyword = request.getParameter("keyword");
        String difficulty = request.getParameter("difficulty");

        WorkbookDAO dao = new WorkbookDAO();
        List<WorkbookDTO> workbookList;

        // 검색어가 있거나 난이도 필터가 있으면 검색 메서드 호출, 아니면 전체 조회
        if ((keyword != null && !keyword.isEmpty()) || (difficulty != null && !difficulty.equals("ALL"))) {
            workbookList = dao.searchWorkbooks(keyword, difficulty);
        } else {
            workbookList = dao.selectAll();
        }

        if (user != null) {
            List<Long> savedIds = dao.selectSavedWorkbookIds(user.getId());
            request.setAttribute("savedIds", savedIds);
        }

        request.setAttribute("workbooks", workbookList);

        // 검색 조건 유지 (화면에 다시 뿌려주기 위함)
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("searchDifficulty", difficulty);

        request.getRequestDispatcher("/views/main.jsp").forward(request, response);
    }
}