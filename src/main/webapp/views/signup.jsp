<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quizi - 회원가입</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    @keyframes blob {
      0% { transform: translate(0px, 0px) scale(1); }
      33% { transform: translate(30px, -50px) scale(1.1); }
      66% { transform: translate(-20px, 20px) scale(0.9); }
      100% { transform: translate(0px, 0px) scale(1); }
    }
    .animate-blob { animation: blob 7s infinite; }
    .animation-delay-2000 { animation-delay: 2s; }
    .animation-delay-4000 { animation-delay: 4s; }
    .glass-card {
      background: rgba(255, 255, 255, 0.8);
      backdrop-filter: blur(12px);
      border: 1px solid rgba(255, 255, 255, 0.6);
    }
  </style>
</head>
<body class="bg-slate-50 flex items-center justify-center min-h-screen relative overflow-hidden font-sans text-slate-900">

<!-- 배경 효과 -->
<div class="absolute top-0 left-0 w-full h-full overflow-hidden -z-10">
  <div class="absolute top-0 right-1/4 w-96 h-96 bg-green-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob"></div>
  <div class="absolute bottom-0 left-1/4 w-96 h-96 bg-blue-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-2000"></div>
  <div class="absolute top-1/3 left-1/3 w-96 h-96 bg-indigo-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-4000"></div>
</div>

<div class="w-full max-w-md p-8 glass-card rounded-3xl shadow-2xl transition-all duration-500 hover:shadow-indigo-500/10">
  <div class="text-center mb-10">
    <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-green-500 to-emerald-600 text-white font-bold text-2xl mb-4 shadow-lg transform transition hover:scale-110">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
      </svg>
    </div>
    <h2 class="text-3xl font-extrabold tracking-tight text-slate-900">계정 만들기</h2>
    <p class="mt-3 text-sm text-slate-500">Quizi와 함께 학습 효율을 높여보세요.</p>
  </div>

  <form action="${root}/signup" method="post" class="space-y-5">
    <div>
      <label class="block text-sm font-bold text-slate-700 mb-2 ml-1">이름</label>
      <input type="text" name="name" required
             class="w-full rounded-xl border border-slate-200 bg-white/50 px-4 py-3.5 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all hover:bg-white"
             placeholder="홍길동">
    </div>
    <div>
      <label class="block text-sm font-bold text-slate-700 mb-2 ml-1">이메일</label>
      <input type="email" name="email" required
             class="w-full rounded-xl border border-slate-200 bg-white/50 px-4 py-3.5 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all hover:bg-white"
             placeholder="example@email.com">
    </div>
    <div>
      <label class="block text-sm font-bold text-slate-700 mb-2 ml-1">비밀번호</label>
      <input type="password" name="password" required
             class="w-full rounded-xl border border-slate-200 bg-white/50 px-4 py-3.5 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all hover:bg-white"
             placeholder="••••••••">
    </div>

    <button type="submit" class="w-full bg-blue-600 text-white font-bold py-4 rounded-xl hover:bg-blue-700 transition-all transform hover:-translate-y-0.5 hover:shadow-lg focus:ring-4 focus:ring-blue-300 mt-4">
      가입 완료하고 시작하기
    </button>
  </form>

  <div class="mt-8 text-center text-sm text-slate-500">
    이미 계정이 있으신가요?
    <a href="${root}/login" class="text-blue-600 font-bold hover:text-blue-500 hover:underline transition-colors ml-1">로그인하기</a>
  </div>
</div>
</body>
</html>