<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quizi - 로그인</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        /* 배경 애니메이션 정의 */
        @keyframes blob {
            0% { transform: translate(0px, 0px) scale(1); }
            33% { transform: translate(30px, -50px) scale(1.1); }
            66% { transform: translate(-20px, 20px) scale(0.9); }
            100% { transform: translate(0px, 0px) scale(1); }
        }
        .animate-blob {
            animation: blob 7s infinite;
        }
        .animation-delay-2000 {
            animation-delay: 2s;
        }
        .animation-delay-4000 {
            animation-delay: 4s;
        }

        /* 유리 질감 효과 */
        .glass-card {
            background: rgba(255, 255, 255, 0.8);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.6);
        }
    </style>
</head>
<body class="bg-slate-50 flex items-center justify-center min-h-screen relative overflow-hidden font-sans text-slate-900">

<!-- 배경 장식 요소 (애니메이션 원) -->
<div class="absolute top-0 left-0 w-full h-full overflow-hidden -z-10">
    <div class="absolute top-0 left-1/4 w-96 h-96 bg-blue-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob"></div>
    <div class="absolute top-0 right-1/4 w-96 h-96 bg-purple-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-2000"></div>
    <div class="absolute -bottom-32 left-1/3 w-96 h-96 bg-pink-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-4000"></div>
</div>

<!-- 메인 카드 -->
<div class="w-full max-w-md p-8 glass-card rounded-3xl shadow-2xl transition-all duration-500 hover:shadow-blue-500/10">
    <div class="text-center mb-10">
        <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-600 to-indigo-600 text-white font-bold text-2xl mb-4 shadow-lg transform transition hover:scale-110">Q</div>
        <h2 class="text-3xl font-extrabold tracking-tight text-slate-900">환영합니다!</h2>
        <p class="mt-3 text-sm text-slate-500">나만의 퀴즈 플랫폼, Quizi에서 학습을 시작하세요.</p>
    </div>

    <!-- 알림 메시지 영역 -->
    <c:if test="${param.msg == 'signup_success'}">
        <div class="mb-6 p-4 bg-green-50 text-green-700 rounded-xl text-sm text-center border border-green-100 flex items-center justify-center gap-2 animate-pulse">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
            회원가입 완료! 로그인해주세요.
        </div>
    </c:if>
    <c:if test="${param.error == 'invalid'}">
        <div class="mb-6 p-4 bg-red-50 text-red-600 rounded-xl text-sm text-center border border-red-100 flex items-center justify-center gap-2">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            이메일 또는 비밀번호를 확인해주세요.
        </div>
    </c:if>

    <form action="${root}/login" method="post" class="space-y-6">
        <div>
            <label class="block text-sm font-bold text-slate-700 mb-2 ml-1">이메일</label>
            <input type="text" name="email" required
                   class="w-full rounded-xl border border-slate-200 bg-white/50 px-4 py-3.5 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all hover:bg-white"
                   placeholder="example@email.com">
        </div>
        <div>
            <div class="flex items-center justify-between mb-2 ml-1">
                <label class="block text-sm font-bold text-slate-700">비밀번호</label>
                <a href="#" class="text-xs font-semibold text-blue-600 hover:text-blue-500">비밀번호 찾기</a>
            </div>
            <input type="password" name="password" required
                   class="w-full rounded-xl border border-slate-200 bg-white/50 px-4 py-3.5 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all hover:bg-white"
                   placeholder="••••••••">
        </div>

        <button type="submit" class="w-full bg-slate-900 text-white font-bold py-4 rounded-xl hover:bg-slate-800 transition-all transform hover:-translate-y-0.5 hover:shadow-lg focus:ring-4 focus:ring-slate-300">
            로그인하기
        </button>
    </form>

    <div class="mt-8 text-center text-sm text-slate-500">
        아직 계정이 없으신가요?
        <a href="${root}/signup" class="text-blue-600 font-bold hover:text-blue-500 hover:underline transition-colors ml-1">무료로 회원가입</a>
    </div>
</div>
</body>
</html>