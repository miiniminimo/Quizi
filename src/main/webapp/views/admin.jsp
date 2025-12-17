<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quizi - 관리자 대시보드</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        .tab-content { display: none; }
        .tab-content.active { display: block; }

        /* 스크롤바 커스텀 */
        ::-webkit-scrollbar { width: 8px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
        ::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
    </style>
</head>
<body class="bg-slate-50 font-sans text-slate-900 h-screen overflow-hidden flex">

<!-- 1. 좌측 사이드바 -->
<aside class="w-64 bg-slate-900 text-white flex flex-col shrink-0 transition-all duration-300">
    <!-- 로고 영역 -->
    <div class="h-20 flex items-center px-8 border-b border-slate-800">
        <div class="flex items-center gap-2 cursor-pointer" onclick="location.href='${root}/main'">
            <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600 text-white font-bold shadow-lg shadow-blue-900/50">Q</div>
            <span class="text-xl font-bold tracking-tight">Quizi</span>
        </div>
    </div>

    <!-- 메뉴 영역 -->
    <nav class="flex-1 px-4 py-8 space-y-2">
        <div class="px-4 mb-2 text-xs font-bold text-slate-500 uppercase tracking-wider">Management</div>

        <button onclick="showTab('users')" id="menu-users" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all bg-blue-600 text-white shadow-lg shadow-blue-900/20">
            <i data-lucide="users" class="h-5 w-5"></i>
            회원 관리
        </button>

        <button onclick="showTab('workbooks')" id="menu-workbooks" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold text-slate-400 hover:bg-slate-800 hover:text-white transition-all">
            <i data-lucide="book-open" class="h-5 w-5"></i>
            문제집 관리
        </button>
    </nav>

    <!-- 하단 프로필/로그아웃 -->
    <div class="p-4 border-t border-slate-800">
        <div class="flex items-center gap-3 px-4 py-3 rounded-xl bg-slate-800/50">
            <div class="h-10 w-10 rounded-full bg-gradient-to-tr from-blue-500 to-violet-500 flex items-center justify-center text-sm font-bold shadow-inner">
                ${sessionScope.user.name.substring(0,1)}
            </div>
            <div class="flex-1 min-w-0">
                <p class="text-sm font-bold truncate">${sessionScope.user.name}</p>
                <p class="text-xs text-slate-400 truncate">Administrator</p>
            </div>
            <a href="${root}/logout" class="text-slate-400 hover:text-red-400 transition-colors" title="로그아웃">
                <i data-lucide="log-out" class="h-5 w-5"></i>
            </a>
        </div>
        <a href="${root}/main" class="block mt-4 text-center text-xs font-bold text-slate-500 hover:text-blue-400 transition-colors">
            <i data-lucide="arrow-left" class="inline h-3 w-3 mr-1"></i> 메인 서비스로 돌아가기
        </a>
    </div>
</aside>

<!-- 2. 우측 메인 컨텐츠 -->
<main class="flex-1 flex flex-col h-full overflow-hidden bg-slate-50 relative">
    <!-- 배경 데코레이션 -->
    <div class="absolute top-0 right-0 w-96 h-96 bg-blue-100/40 rounded-full blur-3xl -mr-20 -mt-20 pointer-events-none"></div>
    <div class="absolute bottom-0 left-0 w-96 h-96 bg-violet-100/40 rounded-full blur-3xl -ml-20 -mb-20 pointer-events-none"></div>

    <!-- 헤더 -->
    <header class="h-20 flex items-center justify-between px-8 border-b border-slate-200/60 bg-white/50 backdrop-blur-xl shrink-0 z-10">
        <h1 class="text-2xl font-bold text-slate-900 flex items-center gap-2">
            <i data-lucide="layout-dashboard" class="h-6 w-6 text-blue-600"></i> Dashboard
        </h1>
        <div class="flex items-center gap-4">
                <span class="text-sm text-slate-500 bg-white px-3 py-1 rounded-full border border-slate-200 shadow-sm">
                    Today: <fmt:formatDate value="<%=new java.util.Date()%>" pattern="yyyy-MM-dd"/>
                </span>
        </div>
    </header>

    <!-- 스크롤 가능한 컨텐츠 영역 -->
    <div class="flex-1 overflow-y-auto p-8">
        <!-- 통계 위젯 -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <!-- 회원 수 카드 -->
            <div class="bg-white p-6 rounded-3xl shadow-sm border border-slate-200 flex flex-col justify-between h-40 relative overflow-hidden group">
                <div class="absolute right-0 top-0 w-32 h-32 bg-blue-50 rounded-full -mr-10 -mt-10 transition-transform group-hover:scale-110"></div>
                <div class="relative z-10">
                    <div class="text-sm font-bold text-slate-500 mb-1">총 회원 수</div>
                    <div class="text-4xl font-black text-slate-900 tracking-tight">${userCount}</div>
                </div>
                <div class="relative z-10 flex items-center text-xs font-bold text-blue-600 bg-blue-50 w-fit px-2 py-1 rounded-lg">
                    <i data-lucide="users" class="h-3 w-3 mr-1"></i> Active Users
                </div>
            </div>

            <!-- 문제집 수 카드 -->
            <div class="bg-white p-6 rounded-3xl shadow-sm border border-slate-200 flex flex-col justify-between h-40 relative overflow-hidden group">
                <div class="absolute right-0 top-0 w-32 h-32 bg-violet-50 rounded-full -mr-10 -mt-10 transition-transform group-hover:scale-110"></div>
                <div class="relative z-10">
                    <div class="text-sm font-bold text-slate-500 mb-1">등록된 문제집</div>
                    <div class="text-4xl font-black text-slate-900 tracking-tight">${workbookCount}</div>
                </div>
                <div class="relative z-10 flex items-center text-xs font-bold text-violet-600 bg-violet-50 w-fit px-2 py-1 rounded-lg">
                    <i data-lucide="book-open" class="h-3 w-3 mr-1"></i> Total Contents
                </div>
            </div>

            <!-- 시스템 상태 (Dummy) -->
            <div class="bg-slate-900 p-6 rounded-3xl shadow-lg flex flex-col justify-between h-40 text-white relative overflow-hidden">
                <div class="absolute inset-0 bg-gradient-to-br from-slate-800 to-slate-900"></div>
                <div class="relative z-10 flex items-center justify-between">
                    <div class="text-sm font-bold text-slate-400">System Status</div>
                    <div class="h-2 w-2 rounded-full bg-green-500 animate-pulse"></div>
                </div>
                <div class="relative z-10">
                    <div class="text-2xl font-bold mb-1">Good</div>
                    <p class="text-xs text-slate-400">All systems operational</p>
                </div>
            </div>
        </div>

        <!-- 1. 회원 관리 테이블 -->
        <div id="tab-users" class="tab-content active animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
                <div class="px-8 py-6 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-lg text-slate-800">회원 목록</h3>
                    <div class="text-xs font-bold text-slate-400 bg-slate-50 px-3 py-1 rounded-full border border-slate-100">
                        Total: ${userCount}
                    </div>
                </div>
                <div class="overflow-x-auto">
                    <table class="w-full text-sm text-left">
                        <thead class="bg-slate-50/50 text-slate-500">
                        <tr>
                            <th class="px-8 py-4 font-bold">ID</th>
                            <th class="px-8 py-4 font-bold">사용자 정보</th>
                            <th class="px-8 py-4 font-bold">이메일</th>
                            <th class="px-8 py-4 font-bold">권한</th>
                            <th class="px-8 py-4 font-bold">가입일</th>
                            <th class="px-8 py-4 font-bold text-right">관리</th>
                        </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100">
                        <c:forEach var="u" items="${allUsers}">
                            <tr class="group hover:bg-slate-50/80 transition-colors">
                                <td class="px-8 py-4 font-mono text-xs text-slate-400">#${u.id}</td>
                                <td class="px-8 py-4">
                                    <div class="flex items-center gap-3">
                                        <div class="h-8 w-8 rounded-full bg-slate-100 flex items-center justify-center text-xs font-bold text-slate-500 group-hover:bg-white group-hover:shadow-sm transition-all">
                                                ${u.name.substring(0,1)}
                                        </div>
                                        <span class="font-bold text-slate-700">${u.name}</span>
                                    </div>
                                </td>
                                <td class="px-8 py-4 text-slate-600">${u.email}</td>
                                <td class="px-8 py-4">
                                            <span class="px-2.5 py-1 rounded-full text-xs font-bold ${u.role == 'ADMIN' ? 'bg-slate-900 text-white' : 'bg-blue-50 text-blue-600 border border-blue-100'}">
                                                    ${u.role}
                                            </span>
                                </td>
                                <td class="px-8 py-4 text-slate-400 font-mono text-xs"><fmt:formatDate value="${u.createdAt}" pattern="yyyy.MM.dd"/></td>
                                <td class="px-8 py-4 text-right">
                                    <c:if test="${u.role != 'ADMIN'}">
                                        <button onclick="deleteItem('deleteUser', ${u.id})" class="text-red-500 hover:text-red-600 hover:bg-red-50 p-2 rounded-lg transition-colors" title="추방">
                                            <i data-lucide="user-x" class="h-4 w-4"></i>
                                        </button>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- 2. 문제집 관리 테이블 -->
        <div id="tab-workbooks" class="tab-content animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
                <div class="px-8 py-6 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-lg text-slate-800">문제집 목록</h3>
                    <div class="text-xs font-bold text-slate-400 bg-slate-50 px-3 py-1 rounded-full border border-slate-100">
                        Total: ${workbookCount}
                    </div>
                </div>
                <div class="overflow-x-auto">
                    <table class="w-full text-sm text-left">
                        <thead class="bg-slate-50/50 text-slate-500">
                        <tr>
                            <th class="px-8 py-4 font-bold">ID</th>
                            <th class="px-8 py-4 font-bold w-1/3">제목</th>
                            <th class="px-8 py-4 font-bold">제작자</th>
                            <th class="px-8 py-4 font-bold">과목</th>
                            <th class="px-8 py-4 font-bold">통계</th>
                            <th class="px-8 py-4 font-bold text-right">관리</th>
                        </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100">
                        <c:forEach var="wb" items="${allWorkbooks}">
                            <tr class="group hover:bg-slate-50/80 transition-colors">
                                <td class="px-8 py-4 font-mono text-xs text-slate-400">#${wb.id}</td>
                                <td class="px-8 py-4">
                                    <div class="font-bold text-slate-800 line-clamp-1">${wb.title}</div>
                                    <div class="text-xs text-slate-400 mt-1 line-clamp-1">${wb.description}</div>
                                </td>
                                <td class="px-8 py-4">
                                    <div class="flex items-center gap-2">
                                        <div class="h-6 w-6 rounded-full bg-slate-100 flex items-center justify-center text-[10px] font-bold text-slate-500">
                                                ${wb.creatorName.substring(0,1)}
                                        </div>
                                        <span class="text-slate-600 font-medium">${wb.creatorName}</span>
                                    </div>
                                </td>
                                <td class="px-8 py-4">
                                    <span class="bg-slate-100 border border-slate-200 text-slate-600 px-2 py-1 rounded-md text-xs font-bold">${wb.subject}</span>
                                </td>
                                <td class="px-8 py-4 text-slate-500 font-mono text-xs">
                                    <span class="text-blue-600 font-bold">${wb.playsCount}</span> views
                                </td>
                                <td class="px-8 py-4 text-right">
                                    <button onclick="deleteItem('deleteWorkbook', ${wb.id})" class="text-red-500 hover:text-red-600 hover:bg-red-50 p-2 rounded-lg transition-colors" title="삭제">
                                        <i data-lucide="trash-2" class="h-4 w-4"></i>
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
    lucide.createIcons();

    function showTab(tabName) {
        // 모든 탭 컨텐츠 숨기기
        document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
        // 선택한 탭 보이기
        document.getElementById('tab-' + tabName).classList.add('active');

        // 메뉴 버튼 스타일 초기화 (기본 스타일)
        const baseClass = "w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold text-slate-400 hover:bg-slate-800 hover:text-white transition-all";
        // 활성화 스타일
        const activeClass = "w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all bg-blue-600 text-white shadow-lg shadow-blue-900/20";

        document.getElementById('menu-users').className = baseClass;
        document.getElementById('menu-workbooks').className = baseClass;

        // 선택한 메뉴에 활성화 스타일 적용
        document.getElementById('menu-' + tabName).className = activeClass;
    }

    function deleteItem(action, id) {
        let msg = action === 'deleteUser' ? '해당 회원을 추방하시겠습니까?' : '해당 문제집을 삭제하시겠습니까?';
        if (!confirm(msg + '\n복구할 수 없습니다.')) return;

        fetch('${root}/admin?action=' + action + '&id=' + id, { method: 'POST' })
            .then(res => {
                if (res.ok) {
                    alert('처리되었습니다.');
                    location.reload();
                } else {
                    res.text().then(text => alert('오류가 발생했습니다: ' + text));
                }
            })
            .catch(() => alert('서버 통신 오류'));
    }
</script>
</body>
</html>