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
import com.quizi.dao.WrongNoteDAO;
import com.quizi.dao.WorkbookDAO;
import com.quizi.dao.SolveHistoryDAO;
import com.quizi.dto.UserDTO;
import com.quizi.dto.WorkbookDTO;

@WebServlet("/mypage")
public class MyPageController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("views/login.jsp");
            return;
        }

        long userId = user.getId();

        WrongNoteDAO wnDao = new WrongNoteDAO();
        List<Map<String, Object>> wrongNotes = wnDao.selectByUserId(userId);

        WorkbookDAO wbDao = new WorkbookDAO();
        List<WorkbookDTO> myWorkbooks = wbDao.selectByCreator(userId);
        // [추가] 저장한 문제집 목록 가져오기
        List<WorkbookDTO> savedWorkbooks = wbDao.selectSavedWorkbooks(userId);

        SolveHistoryDAO shDao = new SolveHistoryDAO();
        List<Map<String, Object>> history = shDao.selectByUserId(userId);

        request.setAttribute("wrongNotes", wrongNotes);
        request.setAttribute("myWorkbooks", myWorkbooks);
        request.setAttribute("savedWorkbooks", savedWorkbooks); // 전달
        request.setAttribute("history", history);

        request.getRequestDispatcher("/views/mypage.jsp").forward(request, response);
    }
}