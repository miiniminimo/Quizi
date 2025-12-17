<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- [핵심] 루트 경로 변수 선언 --%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quizi - 풀이중</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 font-sans text-slate-900">
<!-- [수정] 폼 ID 추가 (자동 제출용) -->
<form id="quizForm" action="${root}/grade" method="post">
  <input type="hidden" name="workbookId" value="${workbook.id}">

  <div class="mx-auto max-w-3xl px-4 py-8">
    <div class="sticky top-0 z-40 mb-8 flex items-center justify-between rounded-2xl border border-slate-200 bg-white/90 p-4 shadow-lg backdrop-blur-md">
      <h2 class="font-bold text-slate-900 truncate max-w-xs pl-2">${workbook.title}</h2>
      <div class="flex items-center gap-4">
        <div class="flex items-center gap-2 text-slate-600 bg-slate-100 px-4 py-2 rounded-full text-sm font-bold font-mono transition-colors" id="timerContainer">
          <i data-lucide="clock" class="h-4 w-4"></i>
          <%-- [수정] 타이머 ID 추가 --%>
          <span id="timerDisplay">${workbook.timeLimit}:00</span>
        </div>
        <button type="submit" class="rounded-full bg-blue-600 px-6 py-2 text-sm font-bold text-white hover:bg-blue-700 shadow-md transition-colors">제출하기</button>
      </div>
    </div>

    <div class="space-y-8">
      <c:forEach var="q" items="${questions}" varStatus="status">
        <div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
          <div class="mb-6 flex items-start justify-between">
            <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-900 text-lg font-bold text-white">${status.index + 1}</span>
            <span class="rounded-lg bg-slate-100 px-3 py-1 text-xs font-bold text-slate-500">${q.score}점</span>
          </div>
          <h3 class="mb-8 text-xl font-bold text-slate-900 leading-relaxed">${q.questionText}</h3>

          <c:choose>
            <c:when test="${q.questionType == 'multiple'}">
              <div class="space-y-3">
                <c:forEach var="opt" items="${q.options}">
                  <label class="group flex cursor-pointer items-center gap-4 rounded-xl border border-slate-100 p-4 transition-all hover:bg-slate-50 hover:border-slate-300">
                    <input type="radio" name="q-${q.id}" value="${opt}" class="h-4 w-4 accent-blue-600">
                    <span class="text-base font-medium text-slate-700">${opt}</span>
                  </label>
                </c:forEach>
              </div>
            </c:when>
            <c:otherwise>
              <input type="text" name="q-${q.id}" placeholder="정답을 입력하세요" class="w-full rounded-xl border border-slate-200 p-4 font-medium focus:border-blue-500 focus:outline-none focus:ring-4 focus:ring-blue-100 transition-all">
            </c:otherwise>
          </c:choose>
        </div>
      </c:forEach>
    </div>
  </div>
</form>

<script>
  lucide.createIcons();

  // [추가] 타이머 동작 스크립트
  const timerDisplay = document.getElementById('timerDisplay');
  const timerContainer = document.getElementById('timerContainer');

  // 문제집 제한시간(분)을 가져와 초 단위로 변환 (기본값 60분)
  const limitMinutes = ${workbook.timeLimit > 0 ? workbook.timeLimit : 60};
  let totalSeconds = limitMinutes * 60;

  const interval = setInterval(() => {
    if (totalSeconds <= 0) {
      clearInterval(interval);
      alert("제한 시간이 종료되어 답안을 자동 제출합니다.");
      document.getElementById('quizForm').submit();
      return;
    }

    totalSeconds--;

    // 분:초 계산
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    // 2자리 숫자로 포맷팅 (05:09)
    const fmtMin = minutes < 10 ? "0" + minutes : minutes;
    const fmtSec = seconds < 10 ? "0" + seconds : seconds;

    timerDisplay.innerText = fmtMin + ":" + fmtSec;

    // 1분 미만 남았을 때 빨간색 경고 효과
    if (totalSeconds < 60) {
      timerContainer.classList.remove('bg-slate-100', 'text-slate-600');
      timerContainer.classList.add('bg-red-50', 'text-red-600', 'animate-pulse');
    }
  }, 1000);
</script>
</body>
</html>