<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quizi - ${workbook.title}</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 font-sans text-slate-900">
<nav class="sticky top-0 z-50 w-full border-b border-slate-100 bg-white/80 backdrop-blur-xl">
    <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <div class="flex items-center gap-2 cursor-pointer" onclick="location.href='${root}/main'">
            <span class="text-xl font-bold tracking-tight text-slate-900">Quizi</span>
        </div>
    </div>
</nav>

<div class="mx-auto max-w-4xl px-4 py-8">
    <button onclick="location.href='${root}/main'" class="mb-6 flex items-center text-sm font-bold text-slate-500 hover:text-slate-900 transition-colors">
        <i data-lucide="arrow-left" class="mr-2 h-4 w-4"></i> 목록으로 돌아가기
    </button>

    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-xl">
        <div class="relative border-b border-slate-100 bg-slate-50 p-12 text-center overflow-hidden">
            <div class="relative z-10">
                <span class="mb-6 inline-block rounded-full bg-white px-4 py-1.5 text-sm font-bold text-blue-600 shadow-sm ring-1 ring-blue-100">${workbook.subject}</span>
                <h1 class="text-4xl font-extrabold text-slate-900 mb-4">${workbook.title}</h1>
                <p class="text-lg text-slate-600 max-w-2xl mx-auto">${workbook.description}</p>
            </div>
        </div>
        <div class="p-10 flex flex-col items-center">
            <div class="flex gap-12 text-center mb-10">
                <div>
                    <div class="text-2xl font-bold text-slate-900">${questionCount}</div>
                    <div class="text-xs font-bold text-slate-400 uppercase tracking-wide mt-1">Questions</div>
                </div>
                <div>
                    <div class="text-2xl font-bold text-slate-900">${workbook.timeLimit}m</div>
                    <div class="text-xs font-bold text-slate-400 uppercase tracking-wide mt-1">Time Limit</div>
                </div>
                <div>
                    <div class="text-2xl font-bold text-slate-900">${workbook.difficulty}</div>
                    <div class="text-xs font-bold text-slate-400 uppercase tracking-wide mt-1">Level</div>
                </div>
            </div>

            <div class="flex w-full max-w-md gap-3">
                <button onclick="location.href='${root}/solve?id=${workbook.id}'" class="flex-1 flex items-center justify-center gap-3 rounded-full bg-blue-600 py-4 text-lg font-bold text-white shadow-xl shadow-blue-200 hover:bg-blue-700 hover:scale-105 transition-all">
                    <i data-lucide="play" class="h-6 w-6 fill-current"></i> 지금 바로 풀기
                </button>

                <%-- [수정] 북마크 버튼 --%>
                <button onclick="toggleBookmark(${workbook.id}, this)" class="flex items-center justify-center rounded-full border px-6 transition-colors border-slate-200 bg-white text-slate-400 hover:text-slate-600 hover:border-slate-300">
                    <i data-lucide="bookmark" class="h-6 w-6"></i>
                </button>
            </div>
        </div>
    </div>
</div>
<script>
    lucide.createIcons();
    function toggleBookmark(workbookId, btn) {
        fetch('${root}/bookmark', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ workbookId: workbookId })
        })
            .then(res => res.json())
            .then(data => {
                if(data.saved) {
                    alert("내 문제집에 저장되었습니다.");
                    btn.querySelector('svg').classList.add('fill-blue-600', 'text-blue-600');
                } else {
                    alert("보관함에서 삭제되었습니다.");
                    btn.querySelector('svg').classList.remove('fill-blue-600', 'text-blue-600');
                }
            });
    }
</script>
</body>
</html>