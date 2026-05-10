package com.quizi.util;

import com.quizi.dao.DailyQuizDAO;
import com.quizi.dto.QuestionDTO;
import com.quizi.service.SlackNotifier;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 서버 시작 시 자동으로 등록되는 퀴즈 스케줄러.
 *
 * config.properties의 quiz.interval.minutes 값에 따라 주기적으로 실행됩니다.
 * 각 유저의 주제 기반 문제를 배정하고 Slack으로 전송합니다.
 * 웹 페이지의 오늘의 문제와 동일한 문제가 Slack으로 전송됩니다.
 */
@WebListener
public class DailyQuizScheduler implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        int interval = parseIntervalMinutes();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "quiz-scheduler");
            t.setDaemon(true);
            return t;
        });

        // 서버 시작 즉시 1회 실행 후 N분마다 반복
        scheduler.scheduleAtFixedRate(this::sendDailyQuiz, 0, interval, TimeUnit.MINUTES);

        System.out.printf("[QuizScheduler] 퀴즈 전송 예약됨 — 즉시 1회 후 %d분마다 반복%n", interval);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // ──────────────────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────────────────

    private void sendDailyQuiz() {
        try {
            DailyQuizDAO dao = new DailyQuizDAO();
            List<Long> userIds = dao.getAllTopicUserIds();

            if (userIds.isEmpty()) {
                // 주제 설정 유저가 없으면 전체 랜덤 Fallback
                QuestionDTO q = dao.pickQuestionForSlack();
                SlackNotifier.sendDailyQuestion(q);
                return;
            }

            // 유저별로 새 문제 강제 배정 → Slack 전송 (웹도 이 문제를 표시)
            for (Long userId : userIds) {
                Map<String, Object> entry = dao.forceNewEntry(userId);
                if (!entry.isEmpty()) {
                    QuestionDTO q = (QuestionDTO) entry.get("question");
                    boolean sent = SlackNotifier.sendDailyQuestion(q);
                    if (!sent) {
                        System.err.printf("[QuizScheduler] userId=%d Slack 전송 실패%n", userId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[QuizScheduler] 작업 실행 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int parseIntervalMinutes() {
        String val = ConfigManager.getProperty("quiz.interval.minutes");
        if (val == null || val.isBlank()) return 10;
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return 10; }
    }

}
