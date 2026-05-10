<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quizi - 결과</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 font-sans text-slate-900">
<div class="mx-auto max-w-3xl px-4 py-8 pb-32">
  <div class="mb-10 rounded-[2.5rem] bg-slate-900 p-10 text-center text-white shadow-2xl relative overflow-hidden">
    <div class="relative z-10">
      <h2 class="text-lg font-bold text-slate-400 mb-2">학습 결과 리포트</h2>
      <div class="my-8 flex items-baseline justify-center gap-3">
        <span class="text-8xl font-black tracking-tighter text-white">${earnedScore}</span>
        <span class="text-4xl text-slate-500 font-bold">/ ${totalScore}</span>
        <span class="text-xl text-slate-400 font-medium self-end mb-4">점</span>
      </div>
      <div class="flex justify-center gap-4">
        <button onclick="location.href='${root}/main'" class="rounded-full bg-white/10 px-8 py-3 font-bold hover:bg-white/20 transition-colors">메인으로</button>
        <button onclick="location.href='${root}/solve?id=${workbook.id}'" class="rounded-full bg-blue-600 px-8 py-3 font-bold hover:bg-blue-500 transition-colors shadow-lg">다시 풀기</button>
      </div>
    </div>
  </div>

  <div class="space-y-6">
    <c:forEach var="item" items="${details}" varStatus="status">
      <div class="relative rounded-2xl border p-6 transition-all ${item.isCorrect ? 'border-slate-100 bg-white' : 'border-red-100 bg-red-50/30'}">

          <%-- [수정] 체크박스에 데이터 속성 추가 --%>
        <c:if test="${!item.isCorrect}">
          <div class="absolute top-6 right-6 z-10">
            <label class="flex items-center gap-2 cursor-pointer bg-white px-3 py-1.5 rounded-lg border border-red-100 shadow-sm hover:bg-red-50 transition-colors">
              <input type="checkbox" class="wrong-check w-4 h-4 accent-red-500" value="${item.question.id}" data-answer="${item.userAnswer}" checked>
              <span class="text-xs font-bold text-slate-600">오답노트 저장</span>
            </label>
          </div>
        </c:if>

        <div class="mb-3 flex items-center justify-between">
                        <span class="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-sm font-bold ${item.isCorrect ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}">
                            ${item.isCorrect ? '정답' : '오답'}
                        </span>
          <span class="text-xs font-bold text-slate-400">배점 ${item.question.score}</span>
        </div>
        <p class="mb-6 text-lg font-bold text-slate-900 pr-24">Q. ${fn:escapeXml(item.question.questionText)}</p>
        <div class="grid gap-4 sm:grid-cols-2">
          <div class="rounded-xl p-4 ${item.isCorrect ? 'bg-green-50 border border-green-100' : 'bg-white border border-red-100'}">
            <span class="block text-xs font-bold ${item.isCorrect ? 'text-green-500' : 'text-slate-400'} mb-1">나의 답안</span>
            <span class="font-bold ${item.isCorrect ? 'text-green-700' : 'text-red-600'}">${fn:escapeXml(item.userAnswer)}</span>
          </div>
          <div class="rounded-xl bg-blue-50 p-4 border border-blue-100">
            <span class="block text-xs font-bold text-blue-400 mb-1">정답</span>
            <span class="font-bold text-blue-700">${fn:escapeXml(item.question.answerText)}</span>
          </div>
        </div>
        <c:if test="${not empty item.question.explanation}">
          <div class="mt-4 pt-4 border-t border-slate-200/50 text-sm text-slate-600">
            <span class="font-bold mr-2">💡 해설:</span> ${fn:escapeXml(item.question.explanation)}
          </div>
        </c:if>
      </div>
    </c:forEach>
  </div>

  <c:if test="${totalScore > earnedScore}">
    <div class="fixed bottom-8 left-1/2 -translate-x-1/2 z-50">
      <button onclick="saveWrongNotes()" class="flex items-center gap-2 rounded-full bg-slate-900 px-8 py-4 font-bold text-white shadow-xl shadow-slate-900/30 hover:bg-slate-800 hover:scale-105 transition-all">
        <i data-lucide="save" class="h-5 w-5"></i>
        <span>선택한 문제 오답노트에 저장</span>
      </button>
    </div>
  </c:if>
</div>

<script>
  lucide.createIcons();

  function saveWrongNotes() {
    const checkboxes = document.querySelectorAll('.wrong-check:checked');
    if (checkboxes.length === 0) {
      alert('저장할 문제를 선택해주세요.');
      return;
    }

    const data = [];
    checkboxes.forEach(cb => {
      data.push({
        questionId: cb.value,
        userAnswer: cb.dataset.answer
      });
    });

    fetch('${root}/wrongnote/save', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
            .then(res => res.json())
            .then(res => {
              if (res.count > 0) {
                alert(res.count + '개의 문제가 오답노트에 저장되었습니다! \n마이페이지에서 확인해보세요.');
              } else {
                alert('이미 저장된 문제들입니다.');
              }
            })
            .catch(err => {
              console.error(err);
              alert('저장 중 오류가 발생했습니다.');
            });
  }
</script>
</body>
</html>