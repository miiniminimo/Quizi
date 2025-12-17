<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quizi - 학습의 새로운 기준</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        .aurora-bg {
            background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%);
        }
        .glass-card {
            background: rgba(255, 255, 255, 0.7);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.5);
        }
    </style>
</head>
<body class="aurora-bg font-sans text-slate-900 overflow-x-hidden">

<!-- 오로라 배경 효과 -->
<div class="fixed top-0 left-1/2 -translate-x-1/2 w-full h-[800px] pointer-events-none -z-10">
    <div class="absolute top-[-20%] left-[-10%] w-[50%] h-[70%] bg-blue-200/40 rounded-full blur-[120px] mix-blend-multiply opacity-70"></div>
    <div class="absolute top-[-10%] right-[-10%] w-[60%] h-[80%] bg-indigo-200/40 rounded-full blur-[120px] mix-blend-multiply opacity-70"></div>
    <div class="absolute bottom-[0%] left-[20%] w-[40%] h-[50%] bg-violet-200/40 rounded-full blur-[100px] mix-blend-multiply opacity-60"></div>
</div>

<!-- 네비게이션 -->
<nav class="fixed top-0 z-50 w-full border-b border-white/50 bg-white/50 backdrop-blur-xl">
    <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-6 lg:px-8">
        <div class="flex items-center gap-2 cursor-pointer" onclick="location.href='${root}/welcome'">
            <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600 text-white font-bold shadow-lg shadow-blue-600/20">Q</div>
            <span class="text-xl font-bold tracking-tight text-slate-900">Quizi</span>
        </div>
        <div>
            <button onclick="location.href='${root}/login'" class="rounded-full bg-slate-900 px-6 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 hover:shadow-lg">
                로그인 / 시작하기
            </button>
        </div>
    </div>
</nav>

<!-- Hero Section -->
<div class="pt-32 pb-20 sm:pt-40 sm:pb-24 lg:pb-32">
    <div class="mx-auto max-w-7xl px-6 lg:px-8">
        <div class="mx-auto max-w-4xl text-center">
            <div class="mb-8 inline-flex items-center rounded-full border border-blue-200 bg-blue-50/50 px-3 py-1 text-sm font-medium text-blue-700 backdrop-blur-sm">
                <span class="mr-2 inline-block h-2 w-2 animate-pulse rounded-full bg-blue-600"></span>
                AI OCR 기능 업데이트 v2.0
            </div>
            <h1 class="text-5xl font-extrabold tracking-tight text-slate-900 sm:text-7xl leading-tight">
                <span class="block mb-2">당신의 학습 효율을</span>
                <span class="bg-gradient-to-r from-blue-600 to-violet-600 bg-clip-text text-transparent">완벽하게 관리하세요</span>
            </h1>
            <p class="mt-8 text-lg leading-8 text-slate-600 max-w-2xl mx-auto">
                문제집 제작부터 풀이, 자동 채점, 그리고 오답노트까지.<br class="hidden sm:inline"/>
                Quizi은 당신의 성장을 위한 올인원 학습 플랫폼입니다.
            </p>
            <div class="mt-10 flex items-center justify-center gap-x-6">
                <button onclick="location.href='${root}/login'" class="rounded-full bg-blue-600 px-8 py-4 text-base font-bold text-white shadow-xl shadow-blue-600/30 transition-all hover:bg-blue-500 hover:scale-105">
                    무료로 시작하기
                </button>
                <button class="group flex items-center gap-2 rounded-full px-6 py-4 text-base font-semibold text-slate-700 transition-all hover:bg-white/50">
                    기능 살펴보기 <i data-lucide="arrow-right" class="h-4 w-4 transition-transform group-hover:translate-x-1"></i>
                </button>
            </div>
        </div>

        <!-- Dashboard Preview Mockup -->
        <div class="relative mt-20 flow-root sm:mt-28">
            <div class="relative rounded-2xl bg-gray-900/5 p-2 ring-1 ring-inset ring-gray-900/10 lg:-m-4 lg:rounded-3xl lg:p-4 glass-card">
                <div class="rounded-xl bg-white shadow-2xl ring-1 ring-gray-900/10 overflow-hidden">
                    <!-- Browser Header -->
                    <div class="flex items-center gap-2 border-b border-slate-100 bg-slate-50/80 px-4 py-3">
                        <div class="flex gap-1.5">
                            <div class="h-3 w-3 rounded-full bg-red-400"></div>
                            <div class="h-3 w-3 rounded-full bg-amber-400"></div>
                            <div class="h-3 w-3 rounded-full bg-green-400"></div>
                        </div>
                        <div class="mx-auto h-6 w-80 rounded-full bg-white shadow-sm border border-slate-200"></div>
                    </div>
                    <!-- Mock UI -->
                    <div class="flex h-[400px] w-full bg-slate-50/30 p-8 gap-6">
                        <div class="w-64 hidden md:block space-y-4">
                            <div class="h-8 w-32 bg-slate-200 rounded animate-pulse"></div>
                            <div class="h-4 w-full bg-slate-100 rounded mt-8"></div>
                            <div class="h-4 w-3/4 bg-slate-100 rounded"></div>
                        </div>
                        <div class="flex-1 space-y-6">
                            <div class="flex gap-4">
                                <div class="flex-1 h-32 bg-white rounded-2xl border border-slate-100 shadow-sm p-4">
                                    <div class="h-8 w-8 bg-blue-100 rounded mb-4"></div>
                                    <div class="h-4 w-24 bg-slate-100 rounded"></div>
                                </div>
                                <div class="flex-1 h-32 bg-white rounded-2xl border border-slate-100 shadow-sm p-4">
                                    <div class="h-8 w-8 bg-green-100 rounded mb-4"></div>
                                    <div class="h-4 w-24 bg-slate-100 rounded"></div>
                                </div>
                            </div>
                            <div class="h-48 bg-white rounded-2xl border border-slate-100 shadow-sm p-6">
                                <div class="h-6 w-48 bg-slate-100 rounded mb-4"></div>
                                <div class="space-y-2">
                                    <div class="h-2 w-full bg-slate-50 rounded"></div>
                                    <div class="h-2 w-full bg-slate-50 rounded"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Feature Section (Bento Grid) -->
<div class="mx-auto mt-10 max-w-7xl px-6 lg:px-8 pb-32">
    <div class="mx-auto max-w-2xl text-center mb-16">
        <h2 class="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">학습을 위한 완벽한 도구들</h2>
        <p class="mt-4 text-lg text-slate-600">복잡한 과정은 줄이고, 학습에만 집중하세요.</p>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-3 lg:gap-8">
        <!-- Feature 1: OCR -->
        <div class="group relative overflow-hidden rounded-3xl bg-white border border-slate-100 p-8 shadow-sm transition-all hover:shadow-xl hover:-translate-y-1 md:col-span-2">
            <div class="absolute right-0 top-0 -mr-16 -mt-16 h-64 w-64 rounded-full bg-blue-50 blur-3xl transition-all group-hover:bg-blue-100"></div>
            <div class="relative z-10">
                <div class="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-50 text-blue-600">
                    <i data-lucide="scan-line" class="h-6 w-6"></i>
                </div>
                <h3 class="text-2xl font-bold text-slate-900">AI OCR 문제 인식</h3>
                <p class="mt-2 max-w-md text-slate-600">종이 문제집을 찍어서 올리세요. 최신 AI 엔진이 텍스트를 자동으로 인식하여 디지털 문제집으로 변환해 드립니다.</p>
            </div>
        </div>

        <!-- Feature 2: Analytics -->
        <div class="group relative overflow-hidden rounded-3xl bg-slate-900 p-8 text-white shadow-lg transition-all hover:shadow-xl hover:-translate-y-1 md:col-span-1">
            <div class="absolute bottom-0 right-0 -mb-16 -mr-16 h-64 w-64 rounded-full bg-blue-600/30 blur-3xl"></div>
            <div class="relative z-10 h-full flex flex-col justify-between">
                <div>
                    <div class="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white/10 text-white">
                        <i data-lucide="bar-chart-2" class="h-6 w-6"></i>
                    </div>
                    <h3 class="text-2xl font-bold">실시간 분석</h3>
                    <p class="mt-2 text-slate-300">정답률, 취약 유형을 한눈에 파악하세요.</p>
                </div>
            </div>
        </div>

        <!-- Feature 3: Auto Grading -->
        <div class="group relative overflow-hidden rounded-3xl bg-white border border-slate-100 p-8 shadow-sm transition-all hover:shadow-xl hover:-translate-y-1 md:col-span-1">
            <div class="relative z-10">
                <div class="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-green-50 text-green-600">
                    <i data-lucide="check-circle" class="h-6 w-6"></i>
                </div>
                <h3 class="text-xl font-bold text-slate-900">자동 채점</h3>
                <p class="mt-2 text-sm text-slate-600">제출 즉시 결과를 확인하고 상세한 해설을 볼 수 있습니다.</p>
            </div>
        </div>

        <!-- Feature 4: Wrong Note -->
        <div class="group relative overflow-hidden rounded-3xl bg-gradient-to-br from-indigo-50 to-white border border-slate-100 p-8 shadow-sm transition-all hover:shadow-xl hover:-translate-y-1 md:col-span-2">
            <div class="relative z-10 flex flex-col md:flex-row gap-8 items-center">
                <div class="flex-1">
                    <div class="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white text-indigo-600 shadow-sm">
                        <i data-lucide="book-open" class="h-6 w-6"></i>
                    </div>
                    <h3 class="text-2xl font-bold text-slate-900">오답노트 자동화</h3>
                    <p class="mt-2 text-slate-600">틀린 문제만 자동으로 모아줍니다. 시험 직전, 나만의 오답노트로 빈틈없이 대비하세요.</p>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Footer -->
<footer class="border-t border-slate-100 bg-white py-12 text-center text-slate-500 text-sm">
    <p>© 2025 Quizi Platform. All rights reserved.</p>
</footer>

<script>
    lucide.createIcons();
</script>
</body>
</html>