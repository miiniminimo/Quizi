🎓 Quizi - AI 기반 맞춤형 퀴즈 학습 플랫폼

Quizi는 사용자가 직접 문제를 만들거나, 생성형 AI(GenAI) 및 OCR 기술을 활용하여 손쉽게 학습 자료를 디지털화하고 풀이할 수 있는 올인원 학습 플랫폼입니다.

단순한 문제 풀이를 넘어, 오답노트 자동화, 학습 기록 분석, 폴더 관리 등 체계적인 학습 경험을 제공합니다.

✨ 주요 기능 (Key Features)

1. 문제집 생성 (Creation Studio)

📝 직접 만들기: 직관적인 UI를 통해 객관식/주관식 문제를 직접 입력하고 정답 및 해설을 설정할 수 있습니다.

🤖 AI 자동 생성: 공부하고 싶은 **주제(예: "정보처리기사", "한국사")**만 입력하면, AI(GPT-4o mini)가 자동으로 고품질 문제를 출제합니다.

📷 OCR 이미지 변환: 시험지나 교재 사진, PDF 파일을 업로드하면 AI Vision 기술이 텍스트와 문제를 인식하여 디지털 문제집으로 변환해줍니다.

2. 학습 및 평가 (Learning & Assessment)

CBT 환경: 실제 시험과 유사한 타이머 및 UI 환경에서 문제를 풀 수 있습니다.

자동 채점: 제출 즉시 채점이 이루어지며, 점수와 정답/오답 여부, 상세 해설을 확인할 수 있습니다.

오답 노트: 틀린 문제만 선택하여 오답노트에 저장하고, 마이페이지에서 언제든 복습할 수 있습니다.

3. 개인화 및 관리 (Personalization)

📂 폴더 관리: 구글 드라이브 스타일의 드래그 앤 드롭(Drag & Drop) 기능을 지원하여 문제집을 폴더별로 정리할 수 있습니다.

북마크: 관심 있는 다른 사용자의 문제집을 내 보관함에 저장할 수 있습니다.

학습 통계: 내가 푼 문제 수, 정답률, 최근 기록 등을 대시보드에서 한눈에 볼 수 있습니다.

4. 관리자 시스템 (Admin)

대시보드: 전체 회원 수, 문제집 수 등 플랫폼 현황을 모니터링합니다.

콘텐츠 관리: 부적절한 문제집 삭제 및 악성 회원 관리 기능을 제공합니다.

🛠 기술 스택 (Tech Stack)

Frontend

JSP (Java Server Pages): 서버 사이드 렌더링(SSR) 기반의 뷰 템플릿

Tailwind CSS: Utility-first CSS 프레임워크를 활용한 모던하고 반응형 디자인

JavaScript (Vanilla JS): 비동기 통신(Fetch API) 및 동적 DOM 조작 (드래그 앤 드롭 등)

Backend

Java (Jakarta EE): Servlet 기반의 웹 애플리케이션 서버 로직

Apache Tomcat 10.1: 서블릿 컨테이너 (WAS)

MVC Pattern: Model(DTO/DAO) - View(JSP) - Controller(Servlet) 아키텍처 준수

Database

MySQL 8.0: 관계형 데이터베이스 관리 시스템 (RDBMS)

JDBC & Connection Pool: 효율적인 데이터베이스 연결 관리

External APIs & Libraries

OpenAI GPT-4o mini: 문제 자동 생성 및 이미지 분석(Vision)

Gson: JSON 데이터 파싱 및 직렬화

JBCrypt: 사용자 비밀번호 단방향 암호화 (보안)

Apache Commons FileUpload: 멀티파트 파일 업로드 처리

⚙️ 설치 및 실행 방법 (Installation)

1. 사전 요구 사항 (Prerequisites)

JDK 17 이상

Apache Tomcat 10.1 이상

MySQL 8.0

Maven

2. 데이터베이스 설정

MySQL에 접속하여 quiz_platform 데이터베이스를 생성하고 아래 스키마를 적용합니다.

CREATE DATABASE quiz_platform;
USE quiz_platform;

-- (상세 테이블 생성 SQL은 /src/main/resources/db_schema.sql 참조)
-- users, workbooks, questions, question_options, solve_history, wrong_notes, folders 등


3. 프로젝트 설정

이 저장소를 클론합니다.

git clone [https://github.com/your-username/Quizi.git](https://github.com/your-username/Quizi.git)


src/main/resources/config.properties 파일을 생성하고 DB 정보와 API 키를 입력합니다.

db.url=jdbc:mysql://localhost:3306/quiz_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
db.username=your_username
db.password=your_password
openai.api.key=sk-your-openai-api-key


4. 실행

IDE(IntelliJ/Eclipse)에서 프로젝트를 열고 Maven 의존성을 다운로드합니다.

Tomcat 서버 구성을 추가하고 Artifact(Quizi:war exploded)를 배포합니다.

서버 실행 후 http://localhost:8080/Quizi로 접속합니다.

📂 프로젝트 구조 (Directory Structure)

Quizi/
├── src/main/java/com/quizi/
│   ├── controller/      # 서블릿 컨트롤러 (요청 처리)
│   ├── dao/             # 데이터 접근 객체 (DB 통신)
│   ├── dto/             # 데이터 전송 객체 (모델)
│   ├── service/         # 비즈니스 로직 (AI, OCR 처리)
│   └── util/            # 유틸리티 (DB연결, 설정관리)
├── src/main/webapp/
│   ├── views/           # JSP 뷰 파일 (화면)
│   │   ├── main.jsp
│   │   ├── create.jsp
│   │   ├── solve.jsp
│   │   └── ...
│   ├── index.jsp        # 진입점
│   └── WEB-INF/         # 설정 파일
└── pom.xml              # Maven 의존성 관리


💡 개발 포인트 & 트러블슈팅

트랜잭션 처리: 문제집 생성 시 Workbook과 Questions가 동시에 저장되어야 하므로, setAutoCommit(false)를 통해 하나의 트랜잭션으로 묶어 데이터 무결성을 보장했습니다.

Tomcat 10 호환성: javax.* 패키지가 아닌 jakarta.* 패키지를 사용하는 라이브러리(commons-fileupload2, jstl-api 3.0)를 적용하여 최신 톰캣 환경에서의 호환성 문제를 해결했습니다.

AI 응답 파싱: 생성형 AI의 비정형 응답(Markdown 포함 등)을 처리하기 위해, 정규식과 Gson의 유연한 파싱 로직을 구현하여 안정성을 높였습니다.

📝 License

This project is licensed under the MIT License.
