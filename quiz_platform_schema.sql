-- MySQL Workbench Forward Engineering
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema quiz_platform
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema quiz_platform
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `quiz_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `quiz_platform` ;
-- -----------------------------------------------------
-- Table `quiz_platform`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `role` VARCHAR(10) NULL DEFAULT 'USER',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email` (`email` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 11
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;
-- -----------------------------------------------------
-- Table `quiz_platform`.`folders`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`folders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `user_id` (`user_id` ASC) VISIBLE,
  CONSTRAINT `folders_ibfk_1`
    FOREIGN KEY (`user_id`)
    REFERENCES `quiz_platform`.`users` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;
-- -----------------------------------------------------
-- Table `quiz_platform`.`workbooks`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`workbooks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `creator_id` BIGINT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT NULL DEFAULT NULL,
  `subject` VARCHAR(50) NULL DEFAULT NULL,
  `difficulty` ENUM('상', '중', '하') NULL DEFAULT '중',
  `time_limit` INT NULL DEFAULT '60',
  `source_type` ENUM('MANUAL', 'OCR', 'AI') NULL DEFAULT 'MANUAL',
  `likes_count` INT NULL DEFAULT '0',
  `plays_count` INT NULL DEFAULT '0',
  `created_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `folder_id` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `creator_id` (`creator_id` ASC) VISIBLE,
  INDEX `fk_folder` (`folder_id` ASC) VISIBLE,
  CONSTRAINT `fk_folder`
    FOREIGN KEY (`folder_id`)
    REFERENCES `quiz_platform`.`folders` (`id`)
    ON DELETE SET NULL,
  CONSTRAINT `workbooks_ibfk_1`
    FOREIGN KEY (`creator_id`)
    REFERENCES `quiz_platform`.`users` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 43
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;
-- -----------------------------------------------------
-- Table `quiz_platform`.`bookmarks`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`bookmarks` (
  `user_id` BIGINT NOT NULL,
  `workbook_id` BIGINT NOT NULL,
  `created_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `workbook_id`),
  INDEX `workbook_id` (`workbook_id` ASC) VISIBLE,
  CONSTRAINT `bookmarks_ibfk_1`
    FOREIGN KEY (`user_id`)
    REFERENCES `quiz_platform`.`users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `bookmarks_ibfk_2`
    FOREIGN KEY (`workbook_id`)
    REFERENCES `quiz_platform`.`workbooks` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;
-- -----------------------------------------------------
-- Table `quiz_platform`.`questions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`questions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `workbook_id` BIGINT NOT NULL,
  `question_text` TEXT NOT NULL,
  `question_type` ENUM('multiple', 'short', 'essay') NOT NULL,
  `score` INT NULL DEFAULT '10',
  `answer_text` TEXT NULL DEFAULT NULL,
  `explanation` TEXT NULL DEFAULT NULL,
  `created_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `workbook_id` (`workbook_id` ASC) VISIBLE,
  CONSTRAINT `questions_ibfk_1`
    FOREIGN KEY (`workbook_id`)
    REFERENCES `quiz_platform`.`workbooks` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 622
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;
-- -----------------------------------------------------
-- Table `quiz_platform`.`question_options`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`question_options` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `option_text` VARCHAR(255) NOT NULL,
  `option_order` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `question_id` (`question_id` ASC) VISIBLE,
  CONSTRAINT `question_options_ibfk_1`
    FOREIGN KEY (`question_id`)
    REFERENCES `quiz_platform`.`questions` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 365
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;
-- -----------------------------------------------------
-- Table `quiz_platform`.`solve_history`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`solve_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `workbook_id` BIGINT NOT NULL,
  `earned_score` INT NOT NULL,
  `total_score` INT NOT NULL,
  `correct_count` INT NOT NULL,
  `total_count` INT NOT NULL,
  `solved_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `user_id` (`user_id` ASC) VISIBLE,
  INDEX `workbook_id` (`workbook_id` ASC) VISIBLE,
  CONSTRAINT `solve_history_ibfk_1`
    FOREIGN KEY (`user_id`)
    REFERENCES `quiz_platform`.`users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `solve_history_ibfk_2`
    FOREIGN KEY (`workbook_id`)
    REFERENCES `quiz_platform`.`workbooks` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 36
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;
-- -----------------------------------------------------
-- Table `quiz_platform`.`solve_history_details`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`solve_history_details` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `history_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `user_answer` TEXT NULL DEFAULT NULL,
  `is_correct` TINYINT(1) NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `history_id` (`history_id` ASC) VISIBLE,
  INDEX `question_id` (`question_id` ASC) VISIBLE,
  CONSTRAINT `solve_history_details_ibfk_1`
    FOREIGN KEY (`history_id`)
    REFERENCES `quiz_platform`.`solve_history` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `solve_history_details_ibfk_2`
    FOREIGN KEY (`question_id`)
    REFERENCES `quiz_platform`.`questions` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 60
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;
-- -----------------------------------------------------
-- Table `quiz_platform`.`wrong_notes`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`wrong_notes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `user_answer` TEXT NULL DEFAULT NULL,
  `saved_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `unique_wrong_note` (`user_id` ASC, `question_id` ASC) VISIBLE,
  INDEX `question_id` (`question_id` ASC) VISIBLE,
  CONSTRAINT `wrong_notes_ibfk_1`
    FOREIGN KEY (`user_id`)
    REFERENCES `quiz_platform`.`users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `wrong_notes_ibfk_2`
    FOREIGN KEY (`question_id`)
    REFERENCES `quiz_platform`.`questions` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 31
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;
-- -----------------------------------------------------
-- Table `quiz_platform`.`daily_quiz_log`  ← 추가된 테이블
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quiz_platform`.`daily_quiz_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `sent_date` DATE NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uq_sent_date` (`sent_date` ASC) VISIBLE,
  INDEX `question_id` (`question_id` ASC) VISIBLE,
  CONSTRAINT `daily_quiz_log_ibfk_1`
    FOREIGN KEY (`question_id`)
    REFERENCES `quiz_platform`.`questions` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
