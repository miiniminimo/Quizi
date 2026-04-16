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
  <style>
    /* 스크롤바 커스텀 */
    ::-webkit-scrollbar { width: 6px; }
    ::-webkit-scrollbar-track { background: transparent; }
    ::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; }
    ::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
  </style>
</head>
<body class="bg-slate-50 font-sans text-slate-900 h-screen overflow-hidden flex">

<!-- 1. 좌측 사이드바 -->
<aside class="w-64 bg-white border-r border-slate-200 flex flex-col shrink-0 z-20">
  <div class="h-20 flex items-center px-6 cursor-pointer" onclick="location.href='${root}/main'">
    <div class="flex items-center gap-2">
      <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600 text-white font-bold shadow-md">Q</div>
      <span class="text-xl font-bold tracking-tight text-slate-900">Quizi</span>
    </div>
  </div>

  <nav class="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
    <div class="px-4 mb-2 text-xs font-bold text-slate-400 uppercase tracking-wider">Menu</div>
    <button onclick="location.href='${root}/main'" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold bg-blue-50 text-blue-700 hover:bg-blue-100 transition-all">
      <i data-lucide="home" class="h-5 w-5"></i> 홈
    </button>
    <button onclick="location.href='${root}/mypage'" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-50 hover:text-slate-900 transition-all">
      <i data-lucide="user" class="h-5 w-5"></i> 마이페이지
    </button>

    <!-- My Workbooks (사이드바 리스트) -->
    <div class="mt-8 px-4 mb-2 text-xs font-bold text-slate-400 uppercase tracking-wider flex justify-between items-center">
      <span>My Workbooks</span>
      <!-- [수정] + 버튼 클릭 시 페이지 이동 -->
      <button onclick="location.href='${root}/create'" class="text-blue-500 hover:text-blue-700 p-1 rounded hover:bg-blue-50" title="새 문제집 만들기">
        <i data-lucide="plus" class="h-3 w-3"></i>
      </button>
    </div>

    <div class="space-y-1">
      <c:choose>
        <c:when test="${empty myWorkbooks}">
          <div class="px-4 py-3 text-xs text-slate-400">생성한 문제집이 없습니다.</div>
        </c:when>
        <c:otherwise>
          <c:forEach var="wb" items="${myWorkbooks}">
            <!-- 클릭 시 미리보기(Detail) 호출 -->
            <div class="group flex items-center justify-between px-4 py-2.5 rounded-xl text-sm font-medium text-slate-600 hover:bg-slate-50 hover:text-blue-600 transition-all cursor-pointer"
                 onclick="openPreview('${root}/detail?id=${wb.id}')">
              <div class="flex items-center gap-3 overflow-hidden">
                <i data-lucide="file-text" class="h-4 w-4 text-slate-400 group-hover:text-blue-500 shrink-0"></i>
                <span class="truncate">${wb.title}</span>
              </div>
              <!-- 삭제 버튼 (마우스 오버 시 표시) -->
              <button onclick="deleteWorkbook(${wb.id}, event)" class="opacity-0 group-hover:opacity-100 p-1 rounded hover:bg-red-100 text-red-400 hover:text-red-600 transition-all">
                <i data-lucide="x" class="h-3 w-3"></i>
              </button>
            </div>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </div>

    <c:if test="${sessionScope.user.role == 'ADMIN'}">
      <div class="mt-8 px-4 mb-2 text-xs font-bold text-slate-400 uppercase tracking-wider">Admin</div>
      <button onclick="location.href='${root}/admin'" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold text-red-500 hover:bg-red-50 transition-all">
        <i data-lucide="shield" class="h-5 w-5"></i> 관리자 페이지
      </button>
    </c:if>
  </nav>

  <div class="p-4 border-t border-slate-100">
    <div class="flex items-center gap-3 px-2 py-2">
      <div class="h-9 w-9 rounded-full bg-blue-100 flex items-center justify-center text-sm font-bold text-blue-600">
        ${sessionScope.user.name.substring(0,1)}
      </div>
      <div class="flex-1 min-w-0">
        <p class="text-sm font-bold text-slate-700 truncate">${sessionScope.user.name}</p>
        <p class="text-xs text-slate-400 truncate">${sessionScope.user.email}</p>
      </div>
      <a href="${root}/logout" class="text-slate-400 hover:text-red-500 transition-colors" title="로그아웃">
        <i data-lucide="log-out" class="h-5 w-5"></i>
      </a>
    </div>
  </div>
</aside>

<!-- 2. 우측 메인 컨텐츠 -->
<main class="flex-1 flex flex-col h-full overflow-hidden bg-[#F8FAFC] relative">

  <!-- 메인 컨텐츠 영역 -->
  <div id="main-content-area" class="flex flex-col h-full">
    <header class="bg-white/80 backdrop-blur-md border-b border-slate-200 sticky top-0 z-10 shrink-0">
      <div class="flex items-center justify-between px-8 h-20">
        <!-- 상단 탭 메뉴 (페이지 이동 방식) -->
        <div class="flex bg-slate-100 p-1 rounded-xl">
          <!-- 현재 페이지(공유하기)는 활성화 상태 -->
          <button onclick="location.href='${root}/main'" class="px-6 py-2 rounded-lg text-sm font-bold bg-white text-slate-900 shadow-sm transition-all flex items-center gap-2">
            <i data-lucide="globe" class="h-4 w-4"></i> 공유하기
          </button>
          <!-- 클릭 시 create.jsp로 이동 -->
          <button onclick="location.href='${root}/create'" class="px-6 py-2 rounded-lg text-sm font-bold text-slate-500 hover:text-slate-900 transition-all flex items-center gap-2">
            <i data-lucide="pen-tool" class="h-4 w-4"></i> 직접 만들기
          </button>
        </div>

        <!-- 검색창 -->
        <div id="search-container" class="flex-1 max-w-md ml-8">
          <form action="${root}/main" method="get" class="relative">
            <i data-lucide="search" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400"></i>
            <input type="text" name="keyword" value="${searchKeyword}" placeholder="문제집 검색..." class="w-full h-10 rounded-full border border-slate-200 bg-slate-50 pl-10 pr-4 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100 transition-all">
            <input type="hidden" name="difficulty" value="${searchDifficulty}">
          </form>
        </div>
      </div>
    </header>

    <div class="flex-1 overflow-y-auto p-8 relative">

      <div class="animate-in fade-in slide-in-from-bottom-2 duration-300">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-slate-900 flex items-center gap-2">
            <i data-lucide="sparkles" class="h-5 w-5 text-yellow-500 fill-current"></i> 문제집 보기
          </h2>
          <!-- AI/OCR 바로가기 버튼들 -->
          <div class="flex gap-2">
            <button onclick="location.href='${root}/views/ocr_upload.jsp'" class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50 transition-colors">
              <i data-lucide="scan-line" class="h-3.5 w-3.5"></i> OCR 업로드
            </button>
            <button onclick="location.href='${root}/views/ai_setup.jsp'" class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50 transition-colors">
              <i data-lucide="sparkles" class="h-3.5 w-3.5 text-violet-500"></i> AI 생성
            </button>
          </div>
        </div>

        <!-- 문제집 그리드 -->
        <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          <c:forEach var="wb" items="${workbooks}">
            <div class="group relative cursor-pointer overflow-hidden rounded-2xl bg-white border border-slate-200 shadow-sm transition-all hover:-translate-y-1 hover:shadow-md hover:border-blue-200"
                 onclick="openPreview('${root}/detail?id=${wb.id}')">

              <div class="relative aspect-video w-full bg-slate-50 flex items-center justify-center text-slate-300 group-hover:bg-blue-50/30 transition-colors">
                <i data-lucide="layout-grid" class="h-8 w-8 text-slate-300 group-hover:text-blue-300 transition-colors"></i>
                <button onclick="toggleBookmark(${wb.id}, event, this)"
                        class="absolute top-3 right-3 z-10 flex h-8 w-8 items-center justify-center rounded-full bg-white/90 shadow-sm backdrop-blur transition-transform hover:scale-110 active:scale-95">
                  <c:choose>
                    <c:when test="${savedIds.contains(wb.id)}">
                      <i data-lucide="bookmark" class="h-4 w-4 fill-blue-600 text-blue-600"></i>
                    </c:when>
                    <c:otherwise>
                      <i data-lucide="bookmark" class="h-4 w-4 text-slate-400 hover:text-slate-600"></i>
                    </c:otherwise>
                  </c:choose>
                </button>
              </div>

              <div class="p-5">
                <div class="flex items-center justify-between mb-2">
                  <span class="inline-flex items-center rounded-md bg-blue-50 px-2 py-0.5 text-[10px] font-bold text-blue-700 ring-1 ring-inset ring-blue-700/10">${wb.subject}</span>
                  <span class="flex items-center text-[10px] font-bold text-slate-400 bg-slate-100 px-1.5 py-0.5 rounded">${wb.difficulty}</span>
                </div>
                <h3 class="text-base font-bold text-slate-900 group-hover:text-blue-600 transition-colors line-clamp-1 mb-1">${wb.title}</h3>
                <p class="text-xs text-slate-500 line-clamp-2 h-8 mb-4">${wb.description}</p>

                <div class="flex items-center justify-between pt-3 border-t border-slate-50">
                  <div class="flex items-center gap-2">
                    <div class="h-5 w-5 rounded-full bg-slate-100 flex items-center justify-center text-[9px] font-bold text-slate-500">
                        ${wb.creatorName.substring(0,1)}
                    </div>
                    <span class="text-xs font-medium text-slate-500 truncate max-w-[80px]">${wb.creatorName}</span>
                  </div>
                  <div class="flex items-center gap-1 text-xs text-slate-400">
                    <i data-lucide="users" class="h-3 w-3"></i>
                    <span>${wb.playsCount}</span>
                  </div>
                </div>
              </div>
            </div>
          </c:forEach>

          <c:if test="${empty workbooks}">
            <div class="col-span-full py-32 text-center text-slate-400 border-2 border-dashed border-slate-200 rounded-3xl">
              <i data-lucide="search-x" class="h-12 w-12 mb-4 mx-auto opacity-20"></i>
              <p>검색 결과가 없습니다.</p>
            </div>
          </c:if>
        </div>
      </div>
    </div>
  </div>

  <!-- 미리보기 영역 (Iframe) - 문제집 클릭 시 우측에서 열림 -->
  <div id="preview-area" class="hidden flex-col h-full bg-white relative z-20">
    <div class="h-14 border-b border-slate-100 flex items-center px-6 justify-between bg-white/80 backdrop-blur">
      <button onclick="showHome()" class="flex items-center text-sm font-bold text-slate-500 hover:text-slate-900 gap-2">
        <i data-lucide="arrow-left" class="h-4 w-4"></i> 메인으로 돌아가기
      </button>
      <span class="text-sm font-bold text-blue-600 bg-blue-50 px-3 py-1 rounded-full">Preview Mode</span>
    </div>
    <iframe id="content-frame" class="w-full flex-1 border-0" src=""></iframe>
  </div>

</main>

<script>
  // [Bug Fix] Iframe 내부에서 메인 페이지가 열릴 때(중복 사이드바) 최상위 창으로 리다이렉트
  if (window.self !== window.top) {
    window.top.location.href = window.location.href;
  }

  lucide.createIcons();

  function openPreview(url) {
    document.getElementById('main-content-area').style.display = 'none';
    document.getElementById('preview-area').style.display = 'flex';
    document.getElementById('content-frame').src = url;
  }

  function showHome() {
    document.getElementById('preview-area').style.display = 'none';
    document.getElementById('main-content-area').style.display = 'flex';
    document.getElementById('content-frame').src = '';
  }

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
                btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-bookmark h-4 w-4 text-slate-400 hover:text-slate-600"><path d="m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z"/></svg>';
              }
            });
  }

  // [추가] 사이드바 목록 삭제 함수
  function deleteWorkbook(workbookId, event) {
    event.stopPropagation(); // 미리보기 열림 방지
    if(!confirm("정말 삭제하시겠습니까?")) return;

    fetch('${root}/delete?id=' + workbookId, { method: 'POST' })
            .then(res => {
              if(res.ok) {
                alert("삭제되었습니다.");
                location.reload();
              } else {
                alert("삭제 실패");
              }
            });
  }

  window.onload = function() {
    const urlParams = new URLSearchParams(window.location.search);
    const msg = urlParams.get('msg');
    if (msg === 'ai_created') alert('✨ AI 문제집 생성이 완료되었습니다!');
    else if (msg === 'created') alert('✅ 문제집이 성공적으로 저장되었습니다.');
    if (msg) window.history.replaceState({}, document.title, window.location.pathname);
  };
</script>
</body>
</html>