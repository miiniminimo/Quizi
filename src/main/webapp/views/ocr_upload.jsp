<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quizi - OCR 업로드</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 font-sans text-slate-900">
<div class="mx-auto max-w-3xl px-4 py-8">
  <div class="mb-6 flex items-center gap-4">
    <button onclick="location.href='${root}/main'" class="flex items-center text-slate-500 hover:text-slate-900 transition-colors">
      <i data-lucide="arrow-left" class="h-6 w-6"></i>
    </button>
    <h2 class="text-2xl font-bold text-slate-900">문제집 자동 생성 (OCR)</h2>
  </div>

  <!-- 업로드 폼 -->
  <form action="${root}/ocr" method="post" enctype="multipart/form-data" id="uploadForm">
    <div class="rounded-3xl border-2 border-dashed border-slate-200 bg-white p-16 text-center transition-colors hover:border-blue-300 hover:bg-blue-50/10">
      <div class="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-blue-50 text-blue-600 shadow-sm mb-6 ring-1 ring-blue-100">
        <i data-lucide="upload" class="h-8 w-8"></i>
      </div>
      <h3 class="text-xl font-bold text-slate-900">파일을 이곳에 드래그하거나 선택하세요</h3>
      <p class="mt-2 text-slate-500 text-sm">지원 형식: JPG, PNG, PDF (최대 10MB)</p>

      <input type="file" name="file" id="file-upload" class="hidden" accept=".jpg,.jpeg,.png,.pdf" onchange="handleFileSelect(this)">
      <label for="file-upload" class="mt-8 inline-flex cursor-pointer items-center justify-center rounded-full bg-slate-900 px-8 py-3.5 text-sm font-bold text-white shadow-lg shadow-slate-900/10 hover:bg-slate-800 transition-all hover:scale-105">
        파일 선택하기
      </label>

      <!-- 파일 선택 시 보여질 미리보기 영역 -->
      <div id="file-preview" class="hidden mt-8 mx-auto w-full max-w-sm rounded-2xl bg-white border border-slate-100 p-4 shadow-lg flex items-center gap-4">
        <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
          <i data-lucide="file-text" class="h-6 w-6"></i>
        </div>
        <div class="flex-1 overflow-hidden text-left">
          <p id="file-name" class="truncate font-bold text-slate-900"></p>
          <p class="text-xs text-slate-500">분석 준비 완료</p>
        </div>
        <button type="submit" class="text-sm font-bold text-blue-600 hover:bg-blue-50 px-3 py-1.5 rounded-lg transition-colors">
          분석 시작
        </button>
      </div>
    </div>
  </form>

  <!-- 로딩 표시 (기본 숨김) -->
  <div id="loading-overlay" class="hidden fixed inset-0 bg-white/80 backdrop-blur-sm z-50 flex items-center justify-center">
    <div class="text-center">
      <i data-lucide="loader-2" class="h-12 w-12 text-blue-600 animate-spin mx-auto mb-4"></i>
      <h3 class="text-xl font-bold text-slate-900">AI가 문제를 분석하고 있습니다...</h3>
      <p class="text-slate-500">잠시만 기다려주세요.</p>
    </div>
  </div>
</div>

<script>
  lucide.createIcons();

  function handleFileSelect(input) {
    if (input.files && input.files[0]) {
      document.getElementById('file-name').textContent = input.files[0].name;
      document.getElementById('file-preview').classList.remove('hidden');
    }
  }

  document.getElementById('uploadForm').onsubmit = function() {
    document.getElementById('loading-overlay').classList.remove('hidden');
  };
</script>
</body>
</html>