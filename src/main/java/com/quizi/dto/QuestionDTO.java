package com.quizi.dto;

import java.util.List;

public class QuestionDTO {
    private Long id;
    private Long workbookId;
    private String questionText;
    private String questionType; // multiple, short, essay
    private int score;
    private String answerText;
    private String explanation;

    // 객관식 보기를 담을 리스트 (DB 테이블은 분리되어 있지만 DTO는 하나로 관리하면 편함)
    private List<String> options;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkbookId() { return workbookId; }
    public void setWorkbookId(Long workbookId) { this.workbookId = workbookId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}