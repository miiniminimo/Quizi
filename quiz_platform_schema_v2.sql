-- =====================================================
-- quiz_platform 스키마 v2 — 오늘의 문제 기능 재설계
-- 기존 daily_quiz_log 는 DROP 후 재생성합니다.
-- =====================================================

USE `quiz_platform`;

-- 1. 기존 테이블 정리 (의존 순서대로)
DROP TABLE IF EXISTS `daily_quiz_answers`;
DROP TABLE IF EXISTS `daily_quiz_log`;
DROP TABLE IF EXISTS `daily_quiz_topic`;

-- 2. 유저별 주제 설정
CREATE TABLE `daily_quiz_topic` (
  `user_id`    BIGINT        NOT NULL,
  `topic`      VARCHAR(200)  NOT NULL DEFAULT '',
  `updated_at` DATETIME      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_dqtopic_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 3. 유저별 일일 문제 로그 (sent_date 는 CURDATE() 기준)
CREATE TABLE `daily_quiz_log` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `sent_date`   DATE   NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_date` (`user_id`, `sent_date`),
  INDEX `idx_question` (`question_id` ASC),
  CONSTRAINT `fk_dql_user`
    FOREIGN KEY (`user_id`)     REFERENCES `users`     (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_dql_question`
    FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 4. 유저별 오늘의 문제 답변 기록
CREATE TABLE `daily_quiz_answers` (
  `id`          BIGINT     NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT     NOT NULL,
  `log_id`      BIGINT     NOT NULL,
  `user_answer` TEXT       NULL,
  `is_correct`  TINYINT(1) NOT NULL DEFAULT 0,
  `answered_at` DATETIME   NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_answer_log` (`user_id`, `log_id`),
  CONSTRAINT `fk_dqa_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`           (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_dqa_log`
    FOREIGN KEY (`log_id`)  REFERENCES `daily_quiz_log`  (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 6. workbooks 테이블 공개/비공개 컬럼 추가
--    이미 존재하는 경우 오류 발생 → 직접 확인 후 실행하세요
ALTER TABLE `workbooks`
  ADD COLUMN `is_public` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1=공개, 0=비공개'
  AFTER `plays_count`;

-- 기존 문제집 전부 공개로 초기화
UPDATE `workbooks` SET `is_public` = 1 WHERE `is_public` IS NULL;
