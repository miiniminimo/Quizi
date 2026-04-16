# 🎓 Quizi — AI 기반 맞춤형 퀴즈 학습 플랫폼

> 문제집을 직접 만들거나, AI·OCR로 자동 생성하고, CBT 환경에서 풀고, 오답을 분석하는 올인원 학습 플랫폼

---

## 📌 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#️-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [설치 및 실행](#-설치-및-실행)
- [개발 포인트](#-개발-포인트)

---

## ✨ 주요 기능

### 1. 문제집 생성 (Creation Studio)

| 방식 | 설명 |
|------|------|
| 📝 직접 만들기 | 객관식 / 주관식 문제, 정답 및 해설을 직접 입력 |
| 🤖 AI 자동 생성 | 주제(예: "정보처리기사")를 입력하면 GPT-4o mini가 문제를 자동 출제 |
| 📷 OCR 이미지 변환 | 시험지·교재 사진·PDF를 업로드하면 AI Vision이 텍스트를 인식해 디지털 문제집으로 변환 |

### 2. 학습 및 평가 (Learning & Assessment)

- **CBT 환경**: 실제 시험과 유사한 타이머 UI에서 문제 풀이
- **자동 채점**: 제출 즉시 점수·정답 여부·해설 확인
- **오답노트**: 틀린 문제를 저장하고 마이페이지에서 언제든 복습

### 3. 개인화 및 관리 (Personalization)

- **📂 폴더 관리**: 드래그 앤 드롭으로 문제집을 폴더별 정리
- **북마크**: 다른 사용자의 문제집을 내 보관함에 저장
- **학습 통계**: 푼 문제 수, 정답률, 최근 기록을 대시보드에서 조회

### 4. 관리자 시스템 (Admin)

- **대시보드**: 전체 회원 수, 문제집 수 등 플랫폼 현황 모니터링
- **콘텐츠 관리**: 부적절한 문제집 삭제 및 회원 관리

---

## 🛠️ 기술 스택

### Backend
- **Java 17+** / Jakarta EE 10 (Servlet 6.1 기반 MVC)
- **Apache Tomcat 10.1** (WAS)
- **MySQL 8.0** + JDBC (순수 SQL, ORM 미사용)

### Frontend
- **JSP** (서버 사이드 렌더링)
- **Tailwind CSS** (유틸리티 퍼스트 스타일링)
- **Vanilla JS** (Fetch API, 드래그 앤 드롭)

### 외부 라이브러리 및 API
| 라이브러리 | 용도 |
|-----------|------|
| OpenAI GPT-4o mini | 문제 자동 생성 (텍스트 + Vision) |
| Tess4J 5.9 | OCR 텍스트 인식 (한국어·영어) |
| JBCrypt | 비밀번호 단방향 암호화 |
| Gson 2.11 | JSON 파싱 및 직렬화 |
| Commons FileUpload2 | 멀티파트 파일 업로드 처리 |

---

## 📂 프로젝트 구조

```
Quizi/
├── src/main/java/com/quizi/
│   ├── controller/          # Servlet 컨트롤러 (요청 처리)
│   │   ├── LoginController.java
│   │   ├── MainController.java
│   │   ├── CreateController.java
│   │   ├── SolveController.java
│   │   ├── AiGenController.java
│   │   ├── OcrController.java
│   │   ├── AdminController.java
│   │   └── ...
│   ├── dao/                 # 데이터 접근 객체 (DB 쿼리)
│   │   ├── UserDAO.java
│   │   ├── WorkbookDAO.java
│   │   ├── FolderDAO.java
│   │   ├── SolveHistoryDAO.java
│   │   └── WrongNoteDAO.java
│   ├── dto/                 # 데이터 전송 객체 (모델)
│   │   ├── UserDTO.java
│   │   ├── WorkbookDTO.java
│   │   ├── QuestionDTO.java
│   │   └── FolderDTO.java
│   ├── service/             # 비즈니스 로직
│   │   ├── AiService.java   # OpenAI API 연동 (텍스트·이미지)
│   │   └── OCRService.java  # Tess4J OCR 처리
│   └── util/
│       ├── DBConnection.java    # JDBC 연결
│       └── ConfigManager.java   # 환경변수 / 설정 파일 로드
├── src/main/webapp/
│   ├── views/               # JSP 뷰
│   │   ├── main.jsp         # 메인 (문제집 탐색)
│   │   ├── create.jsp       # 문제집 생성
│   │   ├── solve.jsp        # 문제 풀기
│   │   ├── result.jsp       # 채점 결과
│   │   ├── mypage.jsp       # 마이페이지
│   │   ├── admin.jsp        # 관리자 페이지
│   │   └── ...
│   ├── index.jsp            # 진입점 (→ /main 리다이렉트)
│   └── WEB-INF/
│       ├── web.xml
│       └── tessdata/        # OCR 언어 데이터 (kor, eng)
└── pom.xml
```

---

## ⚙️ 설치 및 실행

### 사전 요구 사항

- JDK 17 이상
- Apache Tomcat 10.1 이상
- MySQL 8.0
- Maven 3.6+

### 1. 데이터베이스 설정

```sql
CREATE DATABASE quiz_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quiz_platform;

-- 상세 스키마: /src/main/resources/db_schema.sql 참조
-- 주요 테이블: users, workbooks, questions, question_options,
--              solve_history, wrong_notes, folders, bookmarks
```

### 2. 설정 파일 생성

`src/main/resources/config.properties` 파일을 생성하고 아래 내용을 입력합니다.

```properties
db.url=jdbc:mysql://localhost:3306/quiz_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
db.username=your_db_username
db.password=your_db_password
openai.api.key=sk-your-openai-api-key
```

> **배포 환경**에서는 환경변수(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `OPENAI_API_KEY`)로도 설정 가능합니다. 환경변수가 우선 적용됩니다.

### 3. 빌드 및 실행

```bash
# 의존성 설치 및 WAR 파일 빌드
./mvnw clean package

# IDE 사용 시
# Tomcat 서버 구성 → Artifact(Quizi:war exploded) 배포 → 서버 실행
```

접속: [http://localhost:8080/Quizi](http://localhost:8080/Quizi)

---

## 💡 개발 포인트

### 트랜잭션 처리
문제집 생성·수정 시 `workbooks`와 `questions` 테이블이 함께 변경되므로, `setAutoCommit(false)`로 단일 트랜잭션으로 묶어 데이터 무결성을 보장합니다. 오류 발생 시 `rollback()`으로 원자성을 유지합니다.

### 보안
- 비밀번호는 JBCrypt로 단방향 해싱하여 저장
- API Key·DB 정보는 `config.properties`와 환경변수로 분리 (`ConfigManager`)
- 관리자 권한 체크: `session`의 `user.role == ADMIN` 검증

### AI 응답 파싱
GPT-4o mini의 응답에 포함될 수 있는 Markdown 코드블록(` ```json `)을 정규식으로 제거하고, Gson의 유연한 파싱으로 다양한 JSON 응답 구조에 대응합니다.

### Tomcat 10 호환성
`javax.*` 대신 `jakarta.*` 패키지 기반 라이브러리(`commons-fileupload2-jakarta`, `jstl-api 3.0`)를 사용하여 최신 Tomcat 환경과의 호환성을 확보했습니다.

### 설정 유연성 (ConfigManager)
환경변수를 우선 조회하고, 없으면 `config.properties`를 읽는 방식으로 로컬 개발과 배포 환경을 모두 지원합니다.

---

## 📝 License

This project is licensed under the [MIT License](LICENSE).
