<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- [핵심] 루트 경로 변수 선언 --%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quizi - 로그인</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50 flex items-center justify-center min-h-screen">
<div class="w-full max-w-md bg-white p-8 rounded-3xl shadow-xl">
    <div class="text-center mb-8">
        <h2 class="text-3xl font-bold text-slate-900">로그인</h2>
        <p class="text-slate-500 mt-2">나만의 학습 공간으로 이동합니다.</p>
    </div>

    <!-- 알림 메시지 영역 -->
    <c:if test="${param.msg == 'signup_success'}">
        <div class="mb-4 p-3 bg-green-100 text-green-700 rounded-lg text-sm text-center">회원가입이 완료되었습니다! 로그인해주세요.</div>
    </c:if>
    <c:if test="${param.error == 'invalid'}">
        <div class="mb-4 p-3 bg-red-100 text-red-700 rounded-lg text-sm text-center">이메일 또는 비밀번호가 올바르지 않습니다.</div>
    </c:if>

    <form action="${root}/login" method="post" class="space-y-6">
        <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">이메일</label>
            <!-- [수정됨] type="email" -> type="text" 로 변경하여 admin 같은 일반 ID도 입력 가능하게 함 -->
            <input type="text" name="email" required class="w-full rounded-xl border border-slate-200 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="이메일 또는 아이디 입력">
        </div>
        <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">비밀번호</label>
            <input type="password" name="password" required class="w-full rounded-xl border border-slate-200 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="비밀번호 입력">
        </div>

        <button type="submit" class="w-full bg-slate-900 text-white font-bold py-3 rounded-xl hover:bg-slate-800 transition">로그인하기</button>
    </form>

    <div class="mt-6 text-center text-sm">
        계정이 없으신가요?
        <a href="${root}/signup" class="text-blue-600 font-bold hover:underline">무료로 회원가입</a>
    </div>
</div>
</body>
</html>