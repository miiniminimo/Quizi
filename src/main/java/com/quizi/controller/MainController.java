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
import com.quizi.dao.FolderDAO; // [추가]
import com.quizi.dto.WorkbookDTO;
import com.quizi.dto.UserDTO;
import com.quizi.dto.FolderDTO; // [추가]

@WebServlet("/main")
public class MainController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        String keyword = request.getParameter("keyword");
        String difficulty = request.getParameter("difficulty");
        String folderId = request.getParameter("folderId"); // [추가]

        WorkbookDAO workbookDAO = new WorkbookDAO();
        List<WorkbookDTO> workbookList;

        Long userId = (user != null) ? user.getId() : null;

        // [수정] searchWorkbooks 메서드 하나로 통합 호출 (folderId, userId 전달)
        workbookList = workbookDAO.searchWorkbooks(keyword, difficulty, folderId, userId);

        if (user != null) {
            List<Long> savedIds = workbookDAO.selectSavedWorkbookIds(user.getId());
            request.setAttribute("savedIds", savedIds);

            List<WorkbookDTO> myWorkbooks = workbookDAO.selectByCreator(user.getId());
            request.setAttribute("myWorkbooks", myWorkbooks);

            // [추가] 내 폴더 목록 가져오기
            FolderDAO folderDAO = new FolderDAO();
            List<FolderDTO> folders = folderDAO.selectByUserId(user.getId());
            request.setAttribute("folders", folders);
        }

        request.setAttribute("workbooks", workbookList);
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("searchDifficulty", difficulty);

        request.getRequestDispatcher("/views/main.jsp").forward(request, response);
    }
}