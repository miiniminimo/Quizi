<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quizi - ${mode == 'edit' ? '문제집 수정' : '문제집 만들기'}</title>
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
  <div class="flex items-center justify-between mb-8">
    <h2 class="text-2xl font-bold text-slate-900">${mode == 'edit' ? '문제집 수정하기' : '새 문제집 만들기'}</h2>
    <div class="flex gap-2">
      <button onclick="location.href='${root}/main'" class="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100">취소</button>
      <button onclick="saveWorkbook()" class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-bold text-white hover:bg-blue-700">
        ${mode == 'edit' ? '수정 완료' : '저장 및 게시'}
      </button>
    </div>
  </div>

  <c:if test="${not empty ocrData}">
    <div class="mb-6 rounded-lg bg-green-50 p-4 text-green-800 text-sm font-medium flex items-center gap-2 border border-green-200">
      <i data-lucide="check-circle" class="h-5 w-5"></i>
      <span>OCR/AI 분석이 완료되었습니다. 내용을 검토하고 저장하세요.</span>
    </div>
  </c:if>

  <div class="space-y-8">
    <!-- 기본 정보 -->
    <div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <h3 class="mb-4 text-lg font-bold text-slate-900">기본 정보</h3>
      <div class="grid gap-6">
        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">문제집 제목</label>
          <input id="wb-title" class="w-full rounded-lg border border-slate-200 px-3 py-2" placeholder="예: 2024 정보처리기사 필기">
        </div>
        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">설명</label>
          <input id="wb-desc" class="w-full rounded-lg border border-slate-200 px-3 py-2" placeholder="설명을 입력하세요">
        </div>
        <div class="flex gap-4">
          <div class="flex-1">
            <label class="mb-1 block text-sm font-medium text-slate-700">과목/태그</label>
            <input id="wb-subject" class="w-full rounded-lg border border-slate-200 px-3 py-2">
          </div>
          <div class="w-32">
            <label class="mb-1 block text-sm font-medium text-slate-700">난이도</label>
            <select id="wb-difficulty" class="w-full rounded-lg border border-slate-200 px-3 py-2">
              <option>상</option>
              <option>중</option>
              <option>하</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- 문제 리스트 -->
    <div id="questions-container" class="space-y-6"></div>

    <button onclick="addQuestion()" class="flex w-full items-center justify-center gap-2 rounded-xl border-2 border-dashed border-slate-300 py-8 text-slate-500 hover:border-blue-500 hover:text-blue-500 transition">
      <i data-lucide="plus" class="h-5 w-5"></i> 문제 추가하기
    </button>
  </div>
</div>

<!-- 데이터 전달용 Hidden Divs -->
<div id="data-ocr" style="display:none;"><c:out value="${ocrData}" escapeXml="true" /></div>
<div id="data-edit" style="display:none;"><c:out value="${workbookJson}" escapeXml="true" /></div>

<script>
  lucide.createIcons();
  let questions = [];
  // 수정 모드일 경우 ID 저장
  let workbookId = ${not empty workbookJson ? (workbookJson.contains("id") ? workbookJson.replaceAll(".*\"id\":(\\d+).*", "$1") : 0) : 0};
  // 위 정규식 파싱이 불안정할 수 있으므로, 아래와 같이 안전하게 처리
  let currentMode = '${mode}';
  let currentWorkbookData = null;

  function init() {
    const ocrText = document.getElementById('data-ocr').textContent;
    const editText = document.getElementById('data-edit').textContent;

    let data = null;

    if (editText && editText.trim() !== "") {
      // 수정 모드 데이터 로드
      try { data = JSON.parse(editText); currentWorkbookData = data; } catch(e){}
    } else if (ocrText && ocrText.trim() !== "") {
      // OCR/AI 데이터 로드
      try { data = JSON.parse(ocrText); } catch(e){}
    }

    if (data) {
      document.getElementById('wb-title').value = data.title || '';
      document.getElementById('wb-desc').value = data.description || '';
      document.getElementById('wb-subject').value = data.subject || '';
      document.getElementById('wb-difficulty').value = data.difficulty || '중';
      questions = data.questions || [];
      if (data.id) workbookId = data.id; // ID 설정
    } else {
      addQuestion();
    }
    renderQuestions();
  }

  function renderQuestions() {
    const container = document.getElementById('questions-container');
    container.innerHTML = '';

    questions.forEach((q, idx) => {
      const qDiv = document.createElement('div');
      qDiv.className = 'relative rounded-2xl border border-slate-200 bg-white p-6 shadow-sm';

      qDiv.innerHTML = `
                    <button onclick="removeQuestion(\${idx})" class="absolute right-4 top-4 text-slate-400 hover:text-red-500">
                        <i data-lucide="trash-2" class="h-5 w-5"></i>
                    </button>
                    <div class="mb-4 flex items-center gap-4">
                        <span class="flex h-6 w-6 items-center justify-center rounded-full bg-slate-900 text-xs text-white">\${idx + 1}</span>
                        <select onchange="updateQuestion(\${idx}, 'questionType', this.value); renderQuestions();" class="rounded-md border border-slate-200 text-sm py-1 px-2">
                            <option value="multiple" \${q.questionType === 'multiple' ? 'selected' : ''}>객관식</option>
                            <option value="short" \${q.questionType === 'short' ? 'selected' : ''}>주관식</option>
                        </select>
                        <input type="number" value="\${q.score}" onchange="updateQuestion(\${idx}, 'score', this.value)" class="w-16 rounded-md border border-slate-200 text-sm py-1 px-2 text-right" placeholder="배점"> 점
                    </div>
                    <div class="space-y-4">
                        <input value="\${q.questionText}" onchange="updateQuestion(\${idx}, 'questionText', this.value)" placeholder="문제를 입력하세요" class="w-full font-medium border-b border-slate-200 py-2 focus:outline-none">

                        \${q.questionType === 'multiple' ? `
              <div class="ml-2 space-y-2 border-l-2 border-slate-100 pl-4">
                                \${(q.options || ['', '', '', '']).map((opt, i) => `
                                    <div class="flex items-center gap-2">
                                        <input value="\${opt}" onchange="updateOption(\${idx}, \${i}, this.value)" placeholder="보기 \${i+1}" class="flex-1 rounded-md bg-slate-50 px-3 py-2 text-sm">
                                        <input type="radio" name="ans-\${idx}" \${q.answerText === opt && opt !== '' ? 'checked' : ''} onchange="updateQuestion(\${idx}, 'answerText', '\${opt}')">
                                    </div>
                                `).join('')}
    </div>
      ` : `
      <input value="\${q.answerText}" onchange="updateQuestion(\${idx}, 'answerText', this.value)" placeholder="정답 텍스트를 입력하세요" class="w-full rounded-md bg-green-50 px-3 py-2 text-sm text-green-800 placeholder-green-400 focus:outline-none">
              `}

                        <textarea onchange="updateQuestion(\${idx}, 'explanation', this.value)" placeholder="해설을 입력하세요" rows="2" class="w-full rounded-md bg-slate-50 px-3 py-2 text-sm focus:outline-none">\${q.explanation || ''}</textarea>
                    </div>
                `;
      container.appendChild(qDiv);
    });
    lucide.createIcons();
  }

  function addQuestion() {
    questions.push({ questionText: '', questionType: 'multiple', score: 10, options: ['', '', '', ''], answerText: '', explanation: '' });
    renderQuestions();
  }
  function removeQuestion(idx) { questions.splice(idx, 1); renderQuestions(); }
  function updateQuestion(idx, field, value) { questions[idx][field] = value; }
  function updateOption(qIdx, optIdx, value) {
    if (!questions[qIdx].options) questions[qIdx].options = ['', '', '', ''];
    questions[qIdx].options[optIdx] = value;
  }

  function saveWorkbook() {
    const data = {
      title: document.getElementById('wb-title').value,
      description: document.getElementById('wb-desc').value,
      subject: document.getElementById('wb-subject').value,
      difficulty: document.getElementById('wb-difficulty').value,
      timeLimit: 60,
      questions: questions
    };

    // [수정] 수정 모드라면 ID 포함
    if (currentMode === 'edit' && workbookId > 0) {
      data.id = workbookId;
    }

    if(!data.title || questions.length === 0) {
      alert('제목과 최소 1개의 문제를 입력해주세요.');
      return;
    }

    // [수정] 모드에 따라 엔드포인트 변경 (생성: /create, 수정: /edit)
    const endpoint = currentMode === 'edit' ? '${root}/edit' : '${root}/create';

    fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
            .then(res => {
              if(res.ok) {
                alert('문제집이 ' + (currentMode === 'edit' ? '수정' : '저장') + '되었습니다!');
                location.href = '${root}/main';
              } else {
                alert('처리 중 오류가 발생했습니다.');
              }
            });
  }

  init();
</script>
</body>
</html>
```

#### 4️⃣ `src/main/webapp/views/mypage.jsp` (수정)
마지막으로, 마이페이지의 **"수정"** 버튼이 실제로 수정 페이지로 이동하도록 링크를 연결합니다.

```jsp
<%-- [기존] --%>
<%-- <button class="flex-1 ...">수정</button> --%>

<%-- [수정 후] (마이페이지의 내가 만든 문제 탭 내부) --%>
<button onclick="location.href='${root}/edit?id=${wb.id}'"
        class="flex-1 rounded-lg bg-slate-50 py-2 text-sm font-bold text-slate-600 hover:bg-slate-100 transition-colors">
  수정
</button>