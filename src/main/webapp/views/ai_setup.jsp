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
    <style>
        .input-tab { display: none; }
        .input-tab.active { display: block; }
    </style>
</head>
<body class="bg-slate-50 font-sans text-slate-900">
<div class="mx-auto max-w-3xl px-4 py-8">
    <div class="mb-6 flex items-center gap-4">
        <button onclick="location.href='${root}/main'" class="flex items-center text-slate-500 hover:text-slate-900 transition-colors">
            <i data-lucide="arrow-left" class="h-6 w-6"></i>
        </button>
        <h2 class="text-2xl font-bold text-slate-900">AI 맞춤 문제 생성</h2>
    </div>

    <%-- 에러 메시지 표시 --%>
    <%
        String error = request.getParameter("error");
        String errorMsg = null;
        if ("empty".equals(error))        errorMsg = "주제를 입력하거나 파일을 업로드해주세요.";
        else if ("ai_fail".equals(error)) errorMsg = "AI가 문제를 생성하지 못했습니다. 다른 주제나 이미지로 다시 시도해주세요.";
        else if ("db_fail".equals(error)) errorMsg = "문제집 저장에 실패했습니다. 잠시 후 다시 시도해주세요.";
        else if (error != null)           errorMsg = "오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    %>
    <% if (errorMsg != null) { %>
    <div class="mb-4 flex items-center gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
        <i data-lucide="alert-circle" class="h-4 w-4 shrink-0"></i>
        <%= errorMsg %>
    </div>
    <% } %>

    <div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
        <div class="mb-8 text-center">
            <div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-500 to-violet-600 text-white shadow-lg">
                <i data-lucide="wand-2" class="h-8 w-8"></i>
            </div>
            <h3 class="text-xl font-bold text-slate-900">어떤 문제를 만들까요?</h3>
            <p class="mt-2 text-sm text-slate-500">주제를 입력하거나, 시험지·교재 이미지를 업로드하면 AI가 자동으로 문제를 출제합니다.</p>
        </div>

        <!-- 탭 선택 -->
        <div class="flex gap-2 mb-6 p-1 bg-slate-100 rounded-xl">
            <button onclick="switchTab('text')" id="tab-btn-text" class="flex-1 py-2 text-sm font-bold rounded-lg bg-white text-slate-900 shadow-sm transition-all flex items-center justify-center gap-1.5">
                <i data-lucide="type" class="h-3.5 w-3.5"></i> 주제 입력
            </button>
            <button onclick="switchTab('file')" id="tab-btn-file" class="flex-1 py-2 text-sm font-bold rounded-lg text-slate-500 hover:text-slate-900 transition-all flex items-center justify-center gap-1.5">
                <i data-lucide="image" class="h-3.5 w-3.5"></i> 이미지/PDF 업로드
            </button>
        </div>

        <form action="${root}/ai-generate" method="post" enctype="multipart/form-data" id="aiForm" class="space-y-6">

            <!-- 1. 텍스트 입력 탭 -->
            <div id="tab-text" class="input-tab active">
                <label class="mb-2 block text-sm font-bold text-slate-700">주제 또는 텍스트</label>
                <textarea name="topic" id="input-topic" class="w-full rounded-xl border border-slate-200 p-4 focus:border-blue-500 focus:outline-none focus:ring-4 focus:ring-blue-50 h-32 resize-none" placeholder="예: 조선 시대의 역사, 2024년 IT 트렌드, 영어 지문..."></textarea>
            </div>

            <!-- 2. 파일 업로드 탭 -->
            <div id="tab-file" class="input-tab">
                <label class="mb-2 block text-sm font-bold text-slate-700">시험지 / 교재 이미지 업로드</label>
                <p class="mb-3 text-xs text-slate-500">이미지나 PDF를 업로드하면 AI가 내용을 자동으로 인식해 문제집을 만들어드립니다.</p>
                <div class="rounded-xl border-2 border-dashed border-slate-200 bg-slate-50 p-8 text-center hover:bg-blue-50/50 hover:border-blue-300 transition-colors cursor-pointer" onclick="document.getElementById('file-upload').click()">
                    <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 text-blue-600 mb-3">
                        <i data-lucide="scan-line" class="h-6 w-6"></i>
                    </div>
                    <p class="text-sm font-medium text-slate-900">파일을 선택하세요</p>
                    <p class="text-xs text-slate-500 mt-1">PDF, JPG, PNG (최대 10MB)</p>
                    <input type="file" name="file" id="file-upload" class="hidden" accept=".jpg,.jpeg,.png,.pdf" onchange="document.querySelector('#file-name').innerText = this.files[0].name">
                    <p id="file-name" class="mt-4 text-sm font-bold text-blue-600"></p>
                </div>
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

        <div id="loading" class="hidden mt-8 text-center">
            <i data-lucide="loader-2" class="h-8 w-8 text-violet-600 animate-spin mx-auto mb-2"></i>
            <p class="text-violet-600 font-bold">AI가 학습 자료를 분석하여 문제를 만들고 있습니다...</p>
            <p class="text-xs text-slate-400 mt-2">자료 양에 따라 시간이 걸릴 수 있습니다.</p>
        </div>
    </div>
</div>
<script>
    lucide.createIcons();

    const BASE_TAB = 'flex-1 py-2 text-sm font-bold rounded-lg transition-all flex items-center justify-center gap-1.5';
    const ACTIVE_TAB = BASE_TAB + ' bg-white text-slate-900 shadow-sm';
    const INACTIVE_TAB = BASE_TAB + ' text-slate-500 hover:text-slate-900';

    function switchTab(tab) {
        document.querySelectorAll('.input-tab').forEach(el => el.classList.remove('active'));
        document.getElementById('tab-' + tab).classList.add('active');

        const btnText = document.getElementById('tab-btn-text');
        const btnFile = document.getElementById('tab-btn-file');

        if (tab === 'text') {
            btnText.className = ACTIVE_TAB;
            btnFile.className = INACTIVE_TAB;
            document.getElementById('file-upload').value = '';
        } else {
            btnFile.className = ACTIVE_TAB;
            btnText.className = INACTIVE_TAB;
            document.getElementById('input-topic').value = '';
        }
    }

    document.getElementById('aiForm').onsubmit = function() {
        document.getElementById('aiForm').style.display = 'none';
        document.getElementById('loading').classList.remove('hidden');
    };
</script>
</body>
</html>