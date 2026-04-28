package com.quizi.util;

import com.quizi.dao.WorkbookDAO;
import com.quizi.dto.QuestionDTO;
import com.quizi.service.SlackNotifier;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 서버 시작 시 자동으로 등록되는 일일 퀴즈 스케줄러.
 *
 * config.properties에서 읽어오는 값:
 *   slack.quiz.hour   - 전송 시각 (시, 기본값 9)
 *   slack.quiz.minute - 전송 시각 (분, 기본값 0)
 *
 * 매일 설정된 시각(기본: 09:00 KST)에 DB에서 무작위 문제를 뽑아 Slack으로 전송합니다.
 */
@WebListener
public class DailyQuizScheduler implements ServletContextListener {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "daily-quiz-scheduler");
            t.setDaemon(true);
            return t;
        });

        long initialDelay = computeInitialDelay();
        long period = TimeUnit.DAYS.toSeconds(1);

        scheduler.scheduleAtFixedRate(this::sendDailyQuiz, initialDelay, period, TimeUnit.SECONDS);

        ZonedDateTime nextRun = ZonedDateTime.now(KST).plusSeconds(initialDelay);
        System.out.printf("[DailyQuizScheduler] 시작됨. 다음 전송: %s%n", nextRun);
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
            // Slack 채널에 오늘의 예고 문제 전송 (유저별 개인화 문제와는 별개)
            QuestionDTO q = new WorkbookDAO().selectRandomQuestion();
            boolean sent = SlackNotifier.sendDailyQuestion(q);
            if (!sent) {
                System.err.println("[DailyQuizScheduler] Slack 전송 실패 또는 건너뜀.");
            }
        } catch (Exception e) {
            System.err.println("[DailyQuizScheduler] 작업 실행 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 오늘 설정 시각까지 남은 초를 계산합니다.
     * 이미 지났으면 내일 같은 시각까지의 초를 반환합니다.
     */
    private long computeInitialDelay() {
        int targetHour   = parseIntConfig("slack.quiz.hour",   9);
        int targetMinute = parseIntConfig("slack.quiz.minute", 0);

        ZonedDateTime now    = ZonedDateTime.now(KST);
        ZonedDateTime target = now.toLocalDate().atStartOfDay(KST)
                                  .withHour(targetHour)
                                  .withMinute(targetMinute)
                                  .withSecond(0)
                                  .withNano(0);

        if (!target.isAfter(now)) {
            target = target.plusDays(1);
        }

        return target.toEpochSecond() - now.toEpochSecond();
    }

    private int parseIntConfig(String key, int defaultValue) {
        String val = ConfigManager.getProperty(key);
        if (val == null || val.isBlank()) return defaultValue;
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
