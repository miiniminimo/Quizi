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

        <%-- ── 오늘의 문제 배너 (로그인 유저에게만 표시) ── --%>
        <c:if test="${not empty dailyQuestion}">
        <div class="mb-8 rounded-2xl border border-violet-200 bg-gradient-to-br from-violet-50 to-indigo-50 p-6 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <span class="flex h-7 w-7 items-center justify-center rounded-lg bg-violet-600 text-white text-xs font-bold shadow">
                <i data-lucide="calendar-days" class="h-4 w-4"></i>
              </span>
              <span class="text-sm font-bold text-violet-700">오늘의 문제</span>
              <c:if test="${not empty dailyQuestion.workbookTitle}">
                <span class="text-xs text-slate-400">· ${dailyQuestion.workbookTitle}</span>
              </c:if>
            </div>
            <c:choose>
              <c:when test="${not empty dailyAnswer}">
                <c:choose>
                  <c:when test="${dailyQuestion.questionType == 'essay'}">
                    <span class="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-bold text-blue-700 ring-1 ring-blue-200">
                      <i data-lucide="check" class="h-3 w-3 mr-1"></i> 제출 완료
                    </span>
                  </c:when>
                  <c:when test="${dailyAnswer.isCorrect}">
                    <span class="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-bold text-green-700 ring-1 ring-green-200">
                      <i data-lucide="check-circle-2" class="h-3 w-3 mr-1"></i> 정답!
                    </span>
                  </c:when>
                  <c:otherwise>
                    <span class="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-bold text-red-600 ring-1 ring-red-200">
                      <i data-lucide="x-circle" class="h-3 w-3 mr-1"></i> 오답
                    </span>
                  </c:otherwise>
                </c:choose>
              </c:when>
              <c:otherwise>
                <span class="inline-flex items-center rounded-full bg-violet-100 px-2.5 py-0.5 text-xs font-bold text-violet-700 ring-1 ring-violet-200">
                  <i data-lucide="clock" class="h-3 w-3 mr-1"></i> 미답변
                </span>
              </c:otherwise>
            </c:choose>
          </div>

          <%-- 문제 본문 --%>
          <p class="text-base font-semibold text-slate-800 leading-relaxed mb-4">${dailyQuestion.questionText}</p>

          <%-- ── 미답변: 답변 폼 ── --%>
          <c:if test="${empty dailyAnswer}">

            <%-- 객관식 보기 --%>
            <c:if test="${dailyQuestion.questionType == 'multiple' and not empty dailyQuestion.options}">
            <div class="space-y-2 mb-4" id="options-area">
              <c:forEach var="opt" items="${dailyQuestion.options}" varStatus="s">
                <label class="flex items-center gap-3 rounded-xl bg-white/70 border border-violet-100 px-4 py-2.5 text-sm text-slate-700 cursor-pointer hover:border-violet-400 transition-colors has-[:checked]:border-violet-500 has-[:checked]:bg-violet-50">
                  <input type="radio" name="dailyAnswer" value="${opt}" class="accent-violet-600">
                  <span class="font-bold text-violet-500">${s.index + 1}.</span>
                  <span>${opt}</span>
                </label>
              </c:forEach>
            </div>
            </c:if>

            <form action="${root}/daily-quiz/answer" method="post" id="daily-answer-form">
              <input type="hidden" name="logId"        value="${dailyLogId}">
              <input type="hidden" name="answerText"   value="${dailyQuestion.answerText}">
              <input type="hidden" name="questionType" value="${dailyQuestion.questionType}">
              <input type="hidden" name="userAnswer"   id="hidden-user-answer">

              <%-- 단답형/서술형 입력창 --%>
              <c:if test="${dailyQuestion.questionType != 'multiple'}">
                <c:choose>
                  <c:when test="${dailyQuestion.questionType == 'short'}">
                    <input type="text" id="short-answer-input" placeholder="정답을 입력하세요"
                           class="w-full rounded-xl border border-violet-200 px-4 py-3 text-sm focus:outline-none focus:border-violet-500 focus:ring-2 focus:ring-violet-100 mb-4 bg-white">
                  </c:when>
                  <c:otherwise>
                    <textarea id="essay-answer-input" placeholder="답변을 자유롭게 작성하세요" rows="3"
                              class="w-full rounded-xl border border-violet-200 px-4 py-3 text-sm focus:outline-none focus:border-violet-500 focus:ring-2 focus:ring-violet-100 mb-4 bg-white resize-none"></textarea>
                  </c:otherwise>
                </c:choose>
              </c:if>

              <button type="button" onclick="submitDailyAnswer('${dailyQuestion.questionType}')"
                      class="flex items-center gap-1.5 rounded-lg bg-violet-600 px-5 py-2.5 text-sm font-bold text-white hover:bg-violet-700 transition-colors shadow-sm">
                <i data-lucide="send" class="h-4 w-4"></i> 제출하기
              </button>
            </form>

          </c:if>

          <%-- ── 답변 완료: 결과 표시 ── --%>
          <c:if test="${not empty dailyAnswer}">

            <%-- 객관식이면 보기 표시 (내 답 하이라이트) --%>
            <c:if test="${dailyQuestion.questionType == 'multiple' and not empty dailyQuestion.options}">
            <div class="space-y-2 mb-4">
              <c:forEach var="opt" items="${dailyQuestion.options}" varStatus="s">
                <div class="flex items-center gap-3 rounded-xl px-4 py-2.5 text-sm border
                  ${opt == dailyQuestion.answerText ? 'bg-green-50 border-green-300 font-bold text-green-700' :
                    opt == dailyAnswer.userAnswer && opt != dailyQuestion.answerText ? 'bg-red-50 border-red-300 text-red-600 line-through' :
                    'bg-white/50 border-violet-100 text-slate-600'}">
                  <span class="font-bold ${opt == dailyQuestion.answerText ? 'text-green-600' : 'text-violet-400'}">${s.index + 1}.</span>
                  <span>${opt}</span>
                  <c:if test="${opt == dailyQuestion.answerText}">
                    <i data-lucide="check-circle-2" class="h-4 w-4 text-green-500 ml-auto"></i>
                  </c:if>
                  <c:if test="${opt == dailyAnswer.userAnswer and opt != dailyQuestion.answerText}">
                    <i data-lucide="x-circle" class="h-4 w-4 text-red-400 ml-auto"></i>
                  </c:if>
                </div>
              </c:forEach>
            </div>
            </c:if>

            <%-- 단답형/서술형은 내 답변 표시 --%>
            <c:if test="${dailyQuestion.questionType != 'multiple'}">
            <div class="mb-4 rounded-xl border px-4 py-3 text-sm
              ${dailyAnswer.isCorrect ? 'bg-green-50 border-green-200' : 'bg-slate-50 border-slate-200'}">
              <span class="font-bold text-slate-500">내 답변: </span>
              <span class="text-slate-800">${dailyAnswer.userAnswer}</span>
            </div>
            </c:if>

            <%-- 정답 & 해설 --%>
            <div class="space-y-2">
              <div class="flex items-start gap-2 rounded-xl bg-green-50 border border-green-200 px-4 py-3 text-sm">
                <i data-lucide="check-circle-2" class="h-4 w-4 text-green-600 mt-0.5 shrink-0"></i>
                <div>
                  <span class="font-bold text-green-700">정답: </span>
                  <span class="text-slate-800">${dailyQuestion.answerText}</span>
                </div>
              </div>
              <c:if test="${not empty dailyQuestion.explanation}">
              <div class="flex items-start gap-2 rounded-xl bg-blue-50 border border-blue-100 px-4 py-3 text-sm">
                <i data-lucide="lightbulb" class="h-4 w-4 text-blue-500 mt-0.5 shrink-0"></i>
                <div>
                  <span class="font-bold text-blue-600">해설: </span>
                  <span class="text-slate-700">${dailyQuestion.explanation}</span>
                </div>
              </div>
              </c:if>
            </div>

          </c:if>
        </div>
        </c:if>
        <%-- ── 오늘의 문제 배너 끝 ──--%>>

        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-slate-900 flex items-center gap-2">
            <i data-lucide="sparkles" class="h-5 w-5 text-yellow-500 fill-current"></i> 문제집 보기
          </h2>
          <!-- AI 생성 바로가기 버튼 -->
          <div class="flex gap-2">
            <button onclick="location.href='${root}/views/ai_setup.jsp'" class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-violet-600 text-xs font-bold text-white hover:bg-violet-700 transition-colors shadow-sm">
              <i data-lucide="sparkles" class="h-3.5 w-3.5"></i> AI로 문제 만들기
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

  function submitDailyAnswer(type) {
    let answer = '';
    if (type === 'multiple') {
      const checked = document.querySelector('input[name="dailyAnswer"]:checked');
      if (!checked) { alert('보기를 선택해주세요.'); return; }
      answer = checked.value;
    } else if (type === 'short') {
      answer = document.getElementById('short-answer-input').value.trim();
      if (!answer) { alert('답변을 입력해주세요.'); return; }
    } else {
      answer = document.getElementById('essay-answer-input').value.trim();
      if (!answer) { alert('답변을 입력해주세요.'); return; }
    }
    document.getElementById('hidden-user-answer').value = answer;
    document.getElementById('daily-answer-form').submit();
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