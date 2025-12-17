<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection" %>
<%@ page import="com.quizi.util.DBConnection" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quizi - DB Connection Test</title>
  <style>
    body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f8fafc; }
    .card { background: white; padding: 2rem; border-radius: 1rem; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1); text-align: center; }
    .success { color: #16a34a; font-weight: bold; font-size: 1.5rem; }
    .fail { color: #dc2626; font-weight: bold; font-size: 1.5rem; }
    p { color: #64748b; margin-top: 0.5rem; }
  </style>
</head>
<body>
<div class="card">
  <%
    Connection conn = null;
    try {
      conn = DBConnection.getConnection();
      if (conn != null) {
  %>
  <div class="success">✅ 연결 성공!</div>
  <p>MySQL 데이터베이스에 정상적으로 접속되었습니다.</p>
  <p>Project Quizi 준비 완료</p>
  <%
    } else {
      throw new Exception("Connection object is null");
    }
  } catch (Exception e) {
  %>
  <div class="fail">❌ 연결 실패</div>
  <p>오류 내용: <%= e.getMessage() %></p>
  <p>콘솔 로그를 확인해주세요.</p>
  <%
    } finally {
      DBConnection.close(conn);
    }
  %>
</div>
</body>
</html>