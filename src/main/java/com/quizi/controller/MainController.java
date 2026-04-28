package com.quizi.controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.quizi.dao.DailyQuizDAO;
import com.quizi.dao.WorkbookDAO;
import com.quizi.dao.FolderDAO;
import com.quizi.dto.QuestionDTO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dto.UserDTO;
import com.quizi.dto.FolderDTO;

@WebServlet("/main")
public class MainController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        String keyword = request.getParameter("keyword");
        String difficulty = request.getParameter("difficulty");
        String folderId = request.getParameter("folderId");

        WorkbookDAO workbookDAO = new WorkbookDAO();
        List<WorkbookDTO> workbookList;

        Long userId = (user != null) ? user.getId() : null;

        workbookList = workbookDAO.searchWorkbooks(keyword, difficulty, folderId, userId);

        if (user != null) {
            List<Long> savedIds = workbookDAO.selectSavedWorkbookIds(user.getId());
            request.setAttribute("savedIds", savedIds);

            List<WorkbookDTO> myWorkbooks = workbookDAO.selectByCreator(user.getId());
            request.setAttribute("myWorkbooks", myWorkbooks);

            FolderDAO folderDAO = new FolderDAO();
            List<FolderDTO> folders = folderDAO.selectByUserId(user.getId());
            request.setAttribute("folders", folders);
        }

        request.setAttribute("workbooks", workbookList);
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("searchDifficulty", difficulty);

        // 오늘의 문제 (Slack으로 전송된 문제) — 없으면 null (배너 미표시)
        QuestionDTO dailyQuestion = new DailyQuizDAO().getTodayQuestion();
        request.setAttribute("dailyQuestion", dailyQuestion);

        request.getRequestDispatcher("/views/main.jsp").forward(request, response);
    }
}