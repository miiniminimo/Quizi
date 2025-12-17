<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // [수정] 랜딩 페이지 컨트롤러로 이동
    response.sendRedirect(request.getContextPath() + "/welcome");
%>