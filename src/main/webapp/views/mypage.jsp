<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<%
  // 오답노트 그룹화 로직 (이전과 동일)
  java.util.List<java.util.Map<String, Object>> list = (java.util.List<java.util.Map<String, Object>>) request.getAttribute("wrongNotes");
  java.util.Map<String, java.util.List<java.util.Map<String, Object>>> groupedMap = new java.util.LinkedHashMap<>();
  if (list != null) {
    for (java.util.Map<String, Object> note : list) {
      String title = (String) note.get("workbookTitle");
      if (!groupedMap.containsKey(title)) groupedMap.put(title, new java.util.ArrayList<>());
      groupedMap.get(title).add(note);
    }
  }
  request.setAttribute("groupedWrongNotes", groupedMap);
%>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quizi - 마이페이지</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/lucide@latest"></script>
  <style>
    .tab-content { display: none; }
    .tab-content.active { display: block; }
    .accordion-content { transition: max-height 0.3s ease-out; overflow: hidden; max-height: 0; }
    .accordion-content.open { max-height: 2000px; transition: max-height 0.5s ease-in; }
    .rotate-icon { transition: transform 0.3s; }
    .rotate-icon.open { transform: rotate(180deg); }
  </style>
</head>
<body class="bg-slate-50 font-sans text-slate-900">
<nav class="sticky top-0 z-50 w-full border-b border-slate-100 bg-white/80 backdrop-blur-xl">
  <div class="mx-auto flex h-16 max-w-5xl items-center justify-between px-4">
    <div class="flex items-center gap-2 cursor-pointer" onclick="location.href='${root}/main'">
      <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600 text-white font-bold shadow-lg">Q</div>
      <span class="text-xl font-bold tracking-tight text-slate-900">Quiz</span>
    </div>
    <a href="${root}/logout" class="text-sm font-bold text-slate-500 hover:text-red-500">로그아웃</a>
  </div>
</nav>

<div class="mx-auto max-w-5xl px-4 py-8">
  <!-- 1. 프로필 섹션 -->
  <div class="mb-8 flex flex-col md:flex-row items-center gap-8 rounded-3xl border border-slate-100 bg-white p-8 shadow-sm">
    <div class="h-24 w-24 rounded-full bg-slate-900 text-white flex items-center justify-center text-3xl font-bold shadow-xl">
      ${sessionScope.user.name.substring(0,1)}
    </div>
    <div class="text-center md:text-left flex-1">
      <h2 class="text-3xl font-bold text-slate-900">${sessionScope.user.name}</h2>
      <p class="text-slate-500 font-medium">${sessionScope.user.email}</p>
    </div>
    <div class="flex gap-8 text-center">
      <div><div class="text-2xl font-black text-slate-900">${savedWorkbooks.size()}</div><div class="text-xs font-bold text-slate-400">저장</div></div>
      <div><div class="text-2xl font-black text-slate-900">${history.size()}</div><div class="text-xs font-bold text-slate-400">푼 문제집</div></div>
      <div><div class="text-2xl font-black text-red-500">${wrongNotes.size()}</div><div class="text-xs font-bold text-slate-400">오답</div></div>
    </div>
  </div>

  <!-- 주제 설정 -->
  <div class="mb-6 rounded-2xl border border-violet-200 bg-violet-50 p-5">
    <div class="flex items-center gap-2 mb-3">
      <i data-lucide="target" class="h-4 w-4 text-violet-600"></i>
      <span class="text-sm font-bold text-violet-700">오늘의 문제 주제 설정</span>
      <span class="text-xs text-slate-400">설정하면 해당 주제의 문제가 우선 출제됩니다</span>
    </div>
    <form action="${root}/daily-quiz/topic" method="post" class="flex gap-2">
      <input type="text" name="topic" value="${dailyTopic}" placeholder="예: 한국사, 영어, 수학..."
             class="flex-1 rounded-xl border border-violet-200 px-4 py-2 text-sm focus:outline-none focus:border-violet-500 focus:ring-2 focus:ring-violet-100 bg-white">
      <button type="submit" class="rounded-xl bg-violet-600 px-5 py-2 text-sm font-bold text-white hover:bg-violet-700 transition-colors">
        저장
      </button>
      <c:if test="${not empty dailyTopic}">
      <button type="submit" name="topic" value="" class="rounded-xl bg-slate-200 px-4 py-2 text-sm font-bold text-slate-600 hover:bg-slate-300 transition-colors">
        초기화
      </button>
      </c:if>
    </form>
  </div>

  <!-- 2. 탭 메뉴 -->
  <div class="mb-8 flex gap-2 rounded-xl bg-slate-100 p-1 overflow-x-auto">
    <button onclick="showTab('daily')" id="btn-daily" class="flex-1 min-w-[100px] rounded-lg py-2.5 text-sm font-bold bg-white text-slate-900 shadow-sm transition-all">오늘의 문제</button>
    <button onclick="showTab('saved')" id="btn-saved" class="flex-1 min-w-[100px] rounded-lg py-2.5 text-sm font-bold text-slate-500 hover:text-slate-900 transition-all">저장한 문제집</button>
    <button onclick="showTab('wrong')" id="btn-wrong" class="flex-1 min-w-[100px] rounded-lg py-2.5 text-sm font-bold text-slate-500 hover:text-slate-900 transition-all">오답노트</button>
    <button onclick="showTab('history')" id="btn-history" class="flex-1 min-w-[100px] rounded-lg py-2.5 text-sm font-bold text-slate-500 hover:text-slate-900 transition-all">학습 기록</button>
    <button onclick="showTab('created')" id="btn-created" class="flex-1 min-w-[100px] rounded-lg py-2.5 text-sm font-bold text-slate-500 hover:text-slate-900 transition-all">내가 만든 문제</button>
  </div>

  <!-- 3. 탭 컨텐츠 -->

  <!-- [탭 0] 오늘의 문제 기록 -->
  <div id="tab-daily" class="tab-content active">
    <c:choose>
      <c:when test="${empty dailyHistory}">
        <div class="py-20 text-center text-slate-400 border-2 border-dashed border-slate-200 rounded-3xl">
          <i data-lucide="calendar-days" class="h-12 w-12 mb-4 mx-auto opacity-20"></i>
          <p>아직 오늘의 문제 기록이 없습니다.</p>
          <p class="text-xs mt-2">메인 페이지에서 매일 새 문제를 풀어보세요!</p>
        </div>
      </c:when>
      <c:otherwise>
        <div class="space-y-4">
          <c:forEach var="dq" items="${dailyHistory}">
            <div class="rounded-2xl border bg-white p-6 shadow-sm
              ${dq.answeredAt == null ? 'border-slate-200' :
                dq.questionType == 'essay' ? 'border-blue-200' :
                dq.isCorrect ? 'border-green-200' : 'border-red-200'}">
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                  <span class="text-xs font-bold text-slate-400">
                    <fmt:formatDate value="${dq.date}" pattern="yyyy.MM.dd (E)"/>
                  </span>
                  <c:if test="${not empty dq.workbookTitle}">
                    <span class="inline-flex rounded-full bg-violet-50 px-2 py-0.5 text-xs font-bold text-violet-600">${dq.workbookTitle}</span>
                  </c:if>
                </div>
                <c:choose>
                  <c:when test="${dq.answeredAt == null}">
                    <span class="text-xs font-bold text-slate-400 bg-slate-100 px-2 py-0.5 rounded-full">미답변</span>
                  </c:when>
                  <c:when test="${dq.questionType == 'essay'}">
                    <span class="text-xs font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">제출완료</span>
                  </c:when>
                  <c:when test="${dq.isCorrect}">
                    <span class="text-xs font-bold text-green-700 bg-green-50 px-2 py-0.5 rounded-full">✅ 정답</span>
                  </c:when>
                  <c:otherwise>
                    <span class="text-xs font-bold text-red-600 bg-red-50 px-2 py-0.5 rounded-full">❌ 오답</span>
                  </c:otherwise>
                </c:choose>
              </div>

              <p class="font-semibold text-slate-800 mb-3">${dq.questionText}</p>

              <%-- 객관식 보기 --%>
              <c:if test="${dq.questionType == 'multiple' and not empty dq.options}">
              <div class="grid grid-cols-2 gap-1.5 mb-3">
                <c:forEach var="opt" items="${dq.options}" varStatus="s">
                  <div class="rounded-lg px-3 py-1.5 text-xs border
                    ${opt == dq.answerText ? 'bg-green-50 border-green-300 font-bold text-green-700' :
                      opt == dq.userAnswer && opt != dq.answerText ? 'bg-red-50 border-red-200 text-red-500 line-through' :
                      'bg-slate-50 border-slate-100 text-slate-600'}">
                    ${s.index + 1}. ${opt}
                  </div>
                </c:forEach>
              </div>
              </c:if>

              <%-- 내 답변 + 정답 + 해설 --%>
              <c:if test="${dq.answeredAt != null}">
              <div class="mt-3 pt-3 border-t border-slate-100 grid gap-2">
                <c:if test="${dq.questionType != 'multiple'}">
                <div class="text-xs rounded-lg px-3 py-2 ${dq.isCorrect ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-600'}">
                  <span class="font-bold">내 답변: </span>${dq.userAnswer}
                </div>
                </c:if>
                <div class="text-xs rounded-lg bg-green-50 border border-green-100 px-3 py-2 text-green-700">
                  <span class="font-bold">✅ 정답: </span>${dq.answerText}
                </div>
                <c:if test="${not empty dq.explanation}">
                <div class="text-xs rounded-lg bg-blue-50 border border-blue-100 px-3 py-2 text-blue-700">
                  <span class="font-bold">💡 해설: </span>${dq.explanation}
                </div>
                </c:if>
              </div>
              </c:if>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- [탭 1] 저장한 문제집 -->
  <div id="tab-saved" class="tab-content">
    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <c:choose>
        <c:when test="${empty savedWorkbooks}">
          <div class="col-span-full py-20 text-center text-slate-400 border-2 border-dashed border-slate-200 rounded-3xl">
            <i data-lucide="bookmark" class="h-12 w-12 mb-4 mx-auto opacity-20"></i>
            <p>아직 저장한 문제집이 없습니다.</p>
          </div>
        </c:when>
        <c:otherwise>
          <c:forEach var="wb" items="${savedWorkbooks}">
            <div class="group relative cursor-pointer overflow-hidden rounded-2xl border border-slate-200 bg-white transition-all hover:-translate-y-1 hover:shadow-xl"
                 onclick="location.href='${root}/detail?id=${wb.id}'">
              <div class="relative aspect-video w-full bg-slate-50 flex items-center justify-center text-slate-300 group-hover:bg-blue-50/50 transition-colors">
                <i data-lucide="layout-grid" class="h-8 w-8 text-slate-300 group-hover:text-blue-300 transition-colors"></i>
                <button onclick="event.stopPropagation(); toggleBookmark(${wb.id}, this)"
                        class="absolute top-3 right-3 z-10 flex h-8 w-8 items-center justify-center rounded-full bg-white/80 shadow-sm backdrop-blur transition-transform hover:scale-110">
                  <i data-lucide="bookmark" class="h-4 w-4 fill-blue-600 text-blue-600"></i>
                </button>
              </div>
              <div class="p-5">
                <span class="inline-flex items-center rounded-full bg-blue-50 px-2 py-0.5 text-xs font-bold text-blue-700 mb-2">${wb.subject}</span>
                <h3 class="text-base font-bold text-slate-900 group-hover:text-blue-600 transition-colors line-clamp-1 mb-4">${wb.title}</h3>
                <button class="w-full rounded-lg bg-slate-900 py-2 text-sm font-bold text-white hover:bg-slate-800 transition-colors">바로 풀기</button>
              </div>
            </div>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <!-- [탭 2] 오답노트 -->
  <div id="tab-wrong" class="tab-content space-y-6">
    <c:choose>
      <c:when test="${empty groupedWrongNotes}">
        <div class="flex flex-col items-center justify-center py-20 text-slate-400 border-2 border-dashed border-slate-200 rounded-3xl">
          <i data-lucide="check-circle" class="h-12 w-12 mb-4 opacity-20"></i>
          <p>오답노트가 비어있습니다. 완벽하시네요! 🎉</p>
        </div>
      </c:when>
      <c:otherwise>
        <div class="grid gap-6 sm:grid-cols-1 md:grid-cols-2">
          <c:forEach var="entry" items="${groupedWrongNotes}" varStatus="status">
            <div class="rounded-3xl border bg-white p-6 shadow-sm border-slate-200 hover:shadow-md transition-all h-fit">
              <div class="flex items-center justify-between cursor-pointer" onclick="toggleAccordion('acc-${status.index}')">
                <div class="flex-1 pr-4">
                  <div class="flex items-center gap-2 mb-2">
                    <span class="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-bold text-red-600">${entry.value.size()}문제</span>
                    <span class="text-xs font-medium text-slate-400">
                                                <fmt:formatDate value="${entry.value[0].savedAt}" pattern="yyyy.MM.dd"/>
                                            </span>
                  </div>
                  <h4 class="font-bold text-lg text-slate-900 line-clamp-1">${entry.key}</h4>
                </div>
                <button class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-50 transition-transform rotate-icon" id="icon-acc-${status.index}">
                  <i data-lucide="chevron-down" class="h-5 w-5 text-slate-400"></i>
                </button>
              </div>
              <div id="acc-${status.index}" class="accordion-content border-t border-slate-100 mt-4 pt-4">
                <div class="space-y-4">
                  <c:forEach var="note" items="${entry.value}">
                    <div class="rounded-2xl bg-slate-50 p-5">
                      <div class="flex gap-3 mb-3">
                        <div class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-red-500 text-xs font-bold text-white">Q</div>
                        <p class="font-bold text-slate-900 text-sm">${note.questionText}</p>
                      </div>
                      <div class="grid gap-3 sm:grid-cols-2 text-xs">
                        <div class="rounded-lg border border-red-200 bg-white p-2">
                          <span class="block font-bold text-red-400 mb-1">내 답</span>
                          <span class="font-bold text-red-600 line-through">${note.userAnswer}</span>
                        </div>
                        <div class="rounded-lg border border-blue-200 bg-white p-2">
                          <span class="block font-bold text-blue-400 mb-1">정답</span>
                          <span class="font-bold text-blue-600">${note.answerText}</span>
                        </div>
                      </div>
                      <c:if test="${not empty note.explanation}">
                        <div class="mt-3 pt-3 border-t border-slate-200 text-xs text-slate-600">
                          <span class="font-bold mr-1">💡 해설:</span> ${note.explanation}
                        </div>
                      </c:if>
                    </div>
                  </c:forEach>
                  <div class="text-center pt-2">
                    <button onclick="toggleAccordion('acc-${status.index}')" class="text-xs font-bold text-slate-400 hover:text-slate-600">접기</button>
                  </div>
                </div>
              </div>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- [탭 3] 학습 기록 (수정됨: 상세 페이지 이동) -->
  <div id="tab-history" class="tab-content space-y-4">
    <c:if test="${empty history}">
      <div class="py-20 text-center text-slate-400 border-2 border-dashed border-slate-200 rounded-3xl">아직 푼 문제가 없습니다.</div>
    </c:if>
    <c:forEach var="h" items="${history}">
      <!-- [수정] onclick 이벤트 추가: 클릭 시 상세 내역 페이지로 이동 -->
      <div class="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-6 hover:shadow-md transition-all cursor-pointer"
           onclick="location.href='${root}/history/detail?id=${h.id}&title=${h.workbookTitle}'">
        <div>
          <h4 class="font-bold text-slate-900 text-lg mb-1">${h.workbookTitle}</h4>
          <span class="text-xs font-medium text-slate-400"><fmt:formatDate value="${h.solvedAt}" pattern="yyyy.MM.dd HH:mm"/> 풀이</span>
        </div>
        <div class="text-right">
          <div class="text-2xl font-black text-blue-600">${h.earnedScore}점</div>
          <div class="text-xs font-bold text-slate-400">${h.correctCount} / ${h.totalCount} 정답</div>
        </div>
      </div>
    </c:forEach>
  </div>

  <!-- [탭 4] 내가 만든 문제 -->
  <div id="tab-created" class="tab-content">
    <div class="grid gap-4 sm:grid-cols-2">
      <c:choose>
        <c:when test="${empty myWorkbooks}">
          <div class="col-span-full py-20 text-center text-slate-400 border-2 border-dashed border-slate-200 rounded-3xl">
            <i data-lucide="plus-circle" class="h-12 w-12 mb-4 mx-auto opacity-20"></i>
            <p>아직 만든 문제집이 없습니다.</p>
            <button onclick="location.href='${root}/create'" class="mt-4 text-blue-600 font-bold hover:underline">문제집 만들러 가기</button>
          </div>
        </c:when>
        <c:otherwise>
          <c:forEach var="wb" items="${myWorkbooks}">
            <div class="rounded-2xl border border-slate-200 bg-white p-6 transition-all hover:border-blue-300 hover:shadow-md">
              <h4 class="font-bold text-slate-900 text-lg mb-2 line-clamp-1">${wb.title}</h4>
              <p class="text-sm text-slate-500 mb-4 line-clamp-2 h-10">${wb.description}</p>
              <div class="flex gap-2">
                <button onclick="location.href='${root}/detail?id=${wb.id}'" class="flex-1 rounded-lg bg-slate-50 py-2 text-sm font-bold text-slate-600 hover:bg-slate-100 transition-colors">보기</button>
                <button onclick="location.href='${root}/edit?id=${wb.id}'" class="flex-1 rounded-lg bg-blue-50 py-2 text-sm font-bold text-blue-600 hover:bg-blue-100 transition-colors">수정</button>
                <button onclick="deleteWorkbook(${wb.id})" class="flex-1 rounded-lg bg-red-50 py-2 text-sm font-bold text-red-600 hover:bg-red-100 transition-colors">삭제</button>
              </div>
            </div>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>

<script>
  lucide.createIcons();

  function showTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.getElementById('tab-' + tabName).classList.add('active');

    document.querySelectorAll('button[id^="btn-"]').forEach(btn => {
      btn.className = 'flex-1 min-w-[100px] rounded-lg py-2.5 text-sm font-bold text-slate-500 hover:text-slate-900 transition-all';
    });
    const selectedBtn = document.getElementById('btn-' + tabName);
    if(selectedBtn) selectedBtn.className = 'flex-1 min-w-[100px] rounded-lg py-2.5 text-sm font-bold bg-white text-slate-900 shadow-sm transition-all';
  }

  // URL 파라미터로 탭 직접 이동 지원
  const urlTab = new URLSearchParams(window.location.search).get('tab');
  if (urlTab) showTab(urlTab);

  function toggleAccordion(id) {
    const content = document.getElementById(id);
    const icon = document.getElementById('icon-' + id);

    if (content.classList.contains('open')) {
      content.classList.remove('open');
      if(icon) icon.classList.remove('open');
    } else {
      content.classList.add('open');
      if(icon) icon.classList.add('open');
    }
  }

  function toggleBookmark(workbookId, btn) {
    fetch('${root}/bookmark', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ workbookId: workbookId })
    })
            .then(res => res.json())
            .then(data => {
              location.reload();
            });
  }

  function deleteWorkbook(workbookId) {
    if(!confirm("정말 삭제하시겠습니까? 복구할 수 없습니다.")) return;
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
</script>
</body>
</html>