<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- [핵심] 루트 경로 변수 선언 --%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quizi - 회원가입</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50 flex items-center justify-center min-h-screen">
<div class="w-full max-w-md bg-white p-8 rounded-3xl shadow-xl">
  <div class="text-center mb-8">
    <h2 class="text-3xl font-bold text-slate-900">회원가입</h2>
    <p class="text-slate-500 mt-2">지금 가입하고 학습을 시작하세요.</p>
  </div>

  <form action="${root}/signup" method="post" class="space-y-6">
    <div>
      <label class="block text-sm font-medium text-slate-700 mb-1">이름</label>
      <input type="text" name="name" required class="w-full rounded-xl border border-slate-200 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="이름을 입력하세요">
    </div>
    <div>
      <label class="block text-sm font-medium text-slate-700 mb-1">이메일</label>
      <input type="email" name="email" required class="w-full rounded-xl border border-slate-200 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="example@email.com">
    </div>
    <div>
      <label class="block text-sm font-medium text-slate-700 mb-1">비밀번호</label>
      <input type="password" name="password" required class="w-full rounded-xl border border-slate-200 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="********">
    </div>

    <button type="submit" class="w-full bg-blue-600 text-white font-bold py-3 rounded-xl hover:bg-blue-700 transition">회원가입 완료</button>
  </form>

  <div class="mt-6 text-center text-sm">
    이미 계정이 있으신가요?
    <!-- [수정됨] 서블릿 경로로 이동 -->
    <a href="${root}/login" class="text-blue-600 font-bold hover:underline">로그인하기</a>
  </div>
</div>
</body>
</html>