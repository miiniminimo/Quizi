<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quizi - 메인</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 font-sans text-slate-900">
<!-- Navigation (기존 유지) -->
<nav class="sticky top-0 z-50 w-full border-b border-slate-100 bg-white/80 backdrop-blur-xl">
  <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
    <div class="flex items-center gap-2 cursor-pointer" onclick="location.href='${root}/main'">
      <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600 text-white font-bold shadow-lg">Q</div>
      <span class="text-xl font-bold tracking-tight text-slate-900">Quizi</span>
    </div>
    <div class="flex items-center gap-4">
      <div class="flex items-center gap-2 cursor-pointer hover:bg-slate-100 rounded-full py-1 px-2 pr-4 transition-colors" onclick="location.href='${root}/mypage'">
        <div class="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-blue-600 font-bold text-xs">
          ${sessionScope.user.name.substring(0,1)}
        </div>
        <span class="text-sm font-medium text-slate-700 hidden sm:block">${sessionScope.user.name}</span>
      </div>
      <a href="${root}/logout" class="rounded-full bg-slate-100 px-4 py-2 text-xs font-bold text-slate-500 hover:bg-red-50 hover:text-red-500 transition">로그아웃</a>
    </div>
  </div>
</nav>

<!-- Main Content -->
<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
  <!-- Hero Section (기존 유지) -->
  <div class="mb-12 rounded-3xl bg-gradient-to-r from-slate-900 to-slate-800 p-8 text-white shadow-2xl sm:p-16 relative overflow-hidden">
    <div class="relative z-10 max-w-2xl">
      <div class="inline-flex items-center rounded-full bg-white/10 px-3 py-1 text-xs font-medium text-blue-200 ring-1 ring-inset ring-white/20 mb-6">
        <i data-lucide="zap" class="mr-1 h-3 w-3 fill-current"></i> AI Powered
      </div>
      <h2 class="text-4xl font-bold tracking-tight">문제를 찍어서 올려보세요</h2>
      <p class="mt-4 text-lg text-slate-300">AI OCR 기술이 이미지 속 문제를 인식하여<br/>자동으로 문제집을 만들어줍니다.</p>
      <div class="mt-8 flex flex-col sm:flex-row gap-4">
        <button onclick="location.href='${root}/create'" class="flex items-center justify-center gap-2 rounded-full bg-white px-6 py-3.5 text-sm font-bold text-slate-900 shadow-sm hover:bg-slate-50 transition-colors">
          <i data-lucide="plus" class="h-5 w-5"></i> 직접 만들기
        </button>
        <button onclick="location.href='${root}/views/ocr_upload.jsp'" class="flex items-center justify-center gap-2 rounded-full bg-white/10 px-6 py-3.5 text-sm font-bold text-white border border-white/10 hover:bg-white/20 backdrop-blur-sm transition-colors">
          <i data-lucide="scan-line" class="h-5 w-5"></i> 이미지/PDF 업로드
        </button>
        <button onclick="location.href='${root}/views/ai_setup.jsp'" class="flex items-center justify-center gap-2 rounded-full bg-gradient-to-r from-blue-600 to-violet-600 px-6 py-3.5 text-sm font-bold text-white shadow-lg border border-white/10 transition-transform hover:scale-105">
          <i data-lucide="sparkles" class="h-5 w-5"></i> AI 자동 생성
        </button>
      </div>
    </div>
  </div>

  <!-- [수정됨] 검색 및 필터 UI 추가 -->
  <div class="flex flex-col md:flex-row items-center justify-between mb-8 gap-4">
    <h3 class="text-xl font-bold text-slate-900">문제집 탐색</h3>

    <form action="${root}/main" method="get" class="flex w-full md:w-auto gap-2">
      <div class="relative flex-1 md:w-64">
        <i data-lucide="search" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400"></i>
        <input type="text" name="keyword" value="${searchKeyword}"
               placeholder="제목, 과목, 설명 검색"
               class="w-full h-10 rounded-lg border border-slate-200 bg-white pl-10 pr-4 text-sm focus:border-blue-500 focus:outline-none shadow-sm">
      </div>

      <select name="difficulty" class="h-10 rounded-lg border border-slate-200 bg-white px-3 text-sm focus:border-blue-500 focus:outline-none shadow-sm cursor-pointer">
        <option value="ALL">전체 난이도</option>
        <option value="상" ${searchDifficulty == '상' ? 'selected' : ''}>상</option>
        <option value="중" ${searchDifficulty == '중' ? 'selected' : ''}>중</option>
        <option value="하" ${searchDifficulty == '하' ? 'selected' : ''}>하</option>
      </select>

      <button type="submit" class="h-10 px-4 rounded-lg bg-slate-900 text-white text-sm font-bold hover:bg-slate-800 transition-colors shadow-sm">
        검색
      </button>
    </form>
  </div>

  <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
    <c:forEach var="wb" items="${workbooks}">
      <div class="group relative cursor-pointer overflow-hidden rounded-2xl border border-slate-200 bg-white transition-all hover:-translate-y-1 hover:shadow-xl"
           onclick="location.href='${root}/detail?id=${wb.id}'">

        <div class="relative aspect-[16/9] w-full bg-slate-50 flex items-center justify-center text-slate-300 group-hover:bg-blue-50/50 transition-colors">
          <i data-lucide="layout-grid" class="h-10 w-10 text-slate-300 group-hover:text-blue-300 transition-colors"></i>
          <button onclick="toggleBookmark(${wb.id}, event, this)"
                  class="absolute top-4 right-4 z-10 flex h-8 w-8 items-center justify-center rounded-full bg-white/80 shadow-sm backdrop-blur transition-transform hover:scale-110">
            <c:choose>
              <c:when test="${savedIds.contains(wb.id)}">
                <i data-lucide="bookmark" class="h-4 w-4 fill-blue-600 text-blue-600"></i>
              </c:when>
              <c:otherwise>
                <i data-lucide="bookmark" class="h-4 w-4 text-slate-400"></i>
              </c:otherwise>
            </c:choose>
          </button>
        </div>

        <div class="p-6">
          <div class="flex items-center justify-between mb-3">
            <span class="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-bold text-blue-700">${wb.subject}</span>
            <span class="flex items-center text-xs font-medium text-slate-500">${wb.difficulty} 난이도</span>
          </div>
          <h3 class="text-lg font-bold text-slate-900 group-hover:text-blue-600 transition-colors line-clamp-1">${wb.title}</h3>
          <p class="mt-2 text-sm text-slate-500 line-clamp-2">${wb.description}</p>
          <div class="mt-6 flex items-center justify-between border-t border-slate-100 pt-4">
            <div class="flex items-center gap-2">
              <div class="h-6 w-6 rounded-full bg-slate-100 flex items-center justify-center text-[10px] font-bold text-slate-500">${wb.creatorName.substring(0,1)}</div>
              <span class="text-xs font-medium text-slate-500">${wb.creatorName}</span>
            </div>
            <div class="flex items-center gap-1 text-xs text-slate-400">
              <i data-lucide="user" class="h-3 w-3"></i><span>${wb.playsCount}회 풀이</span>
            </div>
          </div>
        </div>
      </div>
    </c:forEach>

    <c:if test="${empty workbooks}">
      <div class="col-span-full py-20 text-center text-slate-400 border-2 border-dashed border-slate-200 rounded-3xl">
        <i data-lucide="search-x" class="h-12 w-12 mb-4 mx-auto opacity-20"></i>
        <p>검색 결과가 없습니다.</p>
      </div>
    </c:if>
  </div>
</div>

<script>
  lucide.createIcons();

  function toggleBookmark(workbookId, event, btn) {
    event.stopPropagation();
    fetch('${root}/bookmark', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ workbookId: workbookId })
    })
            .then(res => res.json())
            .then(data => {
              if(data.saved) {
                btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-bookmark h-4 w-4 fill-blue-600 text-blue-600"><path d="m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z"/></svg>';
              } else {
                btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-bookmark h-4 w-4 text-slate-400"><path d="m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z"/></svg>';
              }
            });
  }
</script>
</body>
</html>