package com.quizi.service;

import com.quizi.dto.QuestionDTO;
import com.quizi.util.ConfigManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Slack Incoming Webhook을 통해 일일 퀴즈 문제를 전송하는 유틸리티 클래스.
 *
 * 사전 설정:
 *   config.properties (또는 환경 변수 SLACK_WEBHOOK_URL)에
 *   slack.webhook.url=https://hooks.slack.com/services/XXX/YYY/ZZZ 형식으로 입력하세요.
 *
 * WorkbookDAO.selectRandomQuestion()이 반환하는 QuestionDTO는
 * questionText 필드 맨 앞에 "\u0000"으로 구분된 workbook title을 포함합니다.
 * 이 클래스가 파싱하여 메시지를 구성합니다.
 */
public class SlackNotifier {

    /** WorkbookDAO가 title과 questionText를 구분하는 데 사용하는 구분자 */
    private static final String TITLE_SEP = "\u0000";

    /**
     * 랜덤으로 선택된 QuestionDTO를 Slack Block Kit 메시지로 포맷하여 전송합니다.
     *
     * @param q WorkbookDAO.selectRandomQuestion()의 반환값
     * @return true = 전송 성공 (HTTP 2xx), false = 실패 또는 webhook URL 미설정
     */
    public static boolean sendDailyQuestion(QuestionDTO q) {
        if (q == null) {
            System.err.println("[SlackNotifier] 전송할 문제가 없습니다 (DB에 문제 없음).");
            return false;
        }

        String webhookUrl = ConfigManager.getProperty("slack.webhook.url");
        if (webhookUrl == null || webhookUrl.isBlank() || webhookUrl.startsWith("https://hooks.slack.com/services/YOUR")) {
            System.err.println("[SlackNotifier] slack.webhook.url이 설정되지 않았습니다. config.properties를 확인하세요.");
            return false;
        }

        // questionText에서 workbook title 파싱
        String rawText = q.getQuestionText();
        String workbookTitle = "";
        String questionText = rawText;
        if (rawText != null && rawText.contains(TITLE_SEP)) {
            int idx = rawText.indexOf(TITLE_SEP);
            workbookTitle = rawText.substring(0, idx);
            questionText = rawText.substring(idx + 1);
        }

        String payload = buildPayload(questionText, workbookTitle, q);

        try {
            postJson(webhookUrl, payload);
            System.out.println("[SlackNotifier] 일일 퀴즈 전송 완료: " + questionText.substring(0, Math.min(30, questionText.length())) + "...");
            return true;
        } catch (Exception e) {
            System.err.println("[SlackNotifier] Slack 전송 중 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────────────────

    private static String buildPayload(String questionText, String workbookTitle, QuestionDTO q) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"blocks\":[");

        // 헤더
        sb.append("{\"type\":\"header\",\"text\":{\"type\":\"plain_text\",\"text\":\"📚 오늘의 Quizi 문제\",\"emoji\":true}},");

        // 문제집 이름 (있을 때만)
        if (!workbookTitle.isBlank()) {
            sb.append("{\"type\":\"context\",\"elements\":[{\"type\":\"mrkdwn\",\"text\":\"*문제집:* ")
              .append(escapeJson(workbookTitle))
              .append("}]},");
        }

        sb.append("{\"type\":\"divider\"},");

        // 문제 본문
        sb.append("{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"*❓ 문제*\\n")
          .append(escapeJson(questionText))
          .append("\"}},");

        // 객관식 보기
        if ("multiple".equals(q.getQuestionType()) && q.getOptions() != null && !q.getOptions().isEmpty()) {
            StringBuilder optSb = new StringBuilder("*보기*\\n");
            String[] nums = {"①", "②", "③", "④", "⑤"};
            List<String> options = q.getOptions();
            for (int i = 0; i < options.size(); i++) {
                String num = (i < nums.length) ? nums[i] : (i + 1) + ".";
                optSb.append(num).append(" ").append(escapeJson(options.get(i)));
                if (i < options.size() - 1) optSb.append("\\n");
            }
            sb.append("{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"")
              .append(optSb)
              .append("\"}},");
        }

        sb.append("{\"type\":\"divider\"},");

        // 정답 & 해설 (접혀있는 느낌을 주기 위해 context 블록 사용)
        StringBuilder answerSb = new StringBuilder("*✅ 정답:* ").append(escapeJson(q.getAnswerText()));
        if (q.getExplanation() != null && !q.getExplanation().isBlank()) {
            answerSb.append("\\n*💡 해설:* ").append(escapeJson(q.getExplanation()));
        }
        sb.append("{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"")
          .append(answerSb)
          .append("\"}}");

        sb.append("]}");
        return sb.toString();
    }

    private static void postJson(String webhookUrl, String payload) throws Exception {
        URL url = new URL(webhookUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new RuntimeException("Slack webhook 응답 오류: HTTP " + status);
        }
    }

    /** JSON 문자열 내 특수문자 이스케이프 */
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
