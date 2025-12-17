<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quizi - AI 문제 생성</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 font-sans text-slate-900">
<div class="mx-auto max-w-3xl px-4 py-8">
    <div class="mb-6 flex items-center gap-4">
        <button onclick="location.href='${root}/main'" class="flex items-center text-slate-500 hover:text-slate-900 transition-colors">
            <i data-lucide="arrow-left" class="h-6 w-6"></i>
        </button>
        <h2 class="text-2xl font-bold text-slate-900">AI 맞춤 문제 생성</h2>
    </div>

    <div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
        <div class="mb-8 text-center">
            <div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-500 to-violet-600 text-white shadow-lg">
                <i data-lucide="wand-2" class="h-8 w-8"></i>
            </div>
            <h3 class="text-xl font-bold text-slate-900">어떤 문제를 만들까요?</h3>
            <p class="mt-2 text-sm text-slate-500">주제나 텍스트를 입력하면 AI가 자동으로 문제를 출제합니다.</p>
        </div>

        <form action="${root}/ai-generate" method="post" id="aiForm" class="space-y-6">
            <div>
                <label class="mb-2 block text-sm font-bold text-slate-700">주제 또는 텍스트 입력</label>
                <textarea name="topic" required class="w-full rounded-xl border border-slate-200 p-4 focus:border-blue-500 focus:outline-none focus:ring-4 focus:ring-blue-50 h-32 resize-none" placeholder="예: 조선 시대의 역사, 피타고라스의 정리, 2024년 IT 트렌드 등..."></textarea>
            </div>

            <div class="grid grid-cols-2 gap-4">
                <div>
                    <label class="mb-2 block text-sm font-bold text-slate-700">문항 수</label>
                    <select name="count" class="w-full rounded-xl border border-slate-200 px-4 py-3 focus:border-blue-500 focus:outline-none">
                        <option value="3">3문제</option>
                        <option value="5" selected>5문제</option>
                        <option value="10">10문제</option>
                    </select>
                </div>
                <div>
                    <label class="mb-2 block text-sm font-bold text-slate-700">난이도</label>
                    <select name="difficulty" class="w-full rounded-xl border border-slate-200 px-4 py-3 focus:border-blue-500 focus:outline-none">
                        <option value="상">상</option>
                        <option value="중" selected>중</option>
                        <option value="하">하</option>
                    </select>
                </div>
            </div>

            <button type="submit" class="w-full flex items-center justify-center gap-2 rounded-xl bg-slate-900 py-4 text-base font-bold text-white shadow-lg shadow-slate-900/20 hover:bg-slate-800 transition-all hover:scale-[1.02]">
                <i data-lucide="sparkles" class="h-5 w-5"></i>
                문제 생성 시작
            </button>
        </form>

        <!-- 로딩 표시 (숨김) -->
        <div id="loading" class="hidden mt-8 text-center">
            <i data-lucide="loader-2" class="h-8 w-8 text-violet-600 animate-spin mx-auto mb-2"></i>
            <p class="text-violet-600 font-bold">AI가 열심히 문제를 만들고 있습니다...</p>
        </div>
    </div>
</div>
<script>
    lucide.createIcons();
    document.getElementById('aiForm').onsubmit = function() {
        document.getElementById('aiForm').style.display = 'none'; // 폼 숨기기
        document.getElementById('loading').classList.remove('hidden'); // 로딩 보이기
    };
</script>
</body>
</html>