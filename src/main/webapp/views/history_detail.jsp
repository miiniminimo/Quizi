<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quizi - 학습 기록 상세</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 font-sans text-slate-900">
<div class="mx-auto max-w-3xl px-4 py-8 pb-32">
    <div class="mb-8 flex items-center justify-between">
        <h2 class="text-2xl font-bold text-slate-900 flex items-center gap-2">
            <i data-lucide="history" class="h-6 w-6 text-blue-500"></i>
            학습 기록 상세
        </h2>
        <button onclick="history.back()" class="rounded-lg bg-white border border-slate-200 px-4 py-2 text-sm font-bold text-slate-600 hover:bg-slate-50 transition-colors">
            돌아가기
        </button>
    </div>

    <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 mb-8 text-center">
        <h3 class="text-lg font-bold text-slate-700 mb-2">${workbookTitle}</h3>
        <p class="text-slate-400 text-sm">지난 풀이 내역입니다.</p>
    </div>

    <div class="space-y-6">
        <c:forEach var="item" items="${historyDetails}" varStatus="status">
            <div class="relative rounded-2xl border p-6 transition-all ${item.isCorrect ? 'border-slate-100 bg-white' : 'border-red-100 bg-red-50/30'}">
                <div class="mb-3 flex items-center justify-between">
                        <span class="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-sm font-bold ${item.isCorrect ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}">
                                ${item.isCorrect ? '정답' : '오답'}
                        </span>
                    <span class="text-xs font-bold text-slate-400">배점 ${item.score}</span>
                </div>
                <p class="mb-6 text-lg font-bold text-slate-900 pr-24">Q. ${item.questionText}</p>
                <div class="grid gap-4 sm:grid-cols-2">
                    <div class="rounded-xl p-4 ${item.isCorrect ? 'bg-slate-50' : 'bg-white border border-red-100'}">
                        <span class="block text-xs font-bold text-slate-400 mb-1">내가 쓴 답</span>
                        <span class="font-bold ${item.isCorrect ? 'text-slate-900' : 'text-red-600'}">${item.userAnswer}</span>
                    </div>
                    <c:if test="${!item.isCorrect}">
                        <div class="rounded-xl bg-blue-50 p-4">
                            <span class="block text-xs font-bold text-blue-400 mb-1">정답</span>
                            <span class="font-bold text-blue-700">${item.answerText}</span>
                        </div>
                    </c:if>
                </div>
                <c:if test="${not empty item.explanation}">
                    <div class="mt-4 pt-4 border-t border-slate-200/50 text-sm text-slate-600">
                        <span class="font-bold mr-2">💡 해설:</span> ${item.explanation}
                    </div>
                </c:if>
            </div>
        </c:forEach>
    </div>
</div>
<script>lucide.createIcons();</script>
</body>
</html>