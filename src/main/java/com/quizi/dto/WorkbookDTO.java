package com.quizi.dto;

import java.sql.Timestamp;
import java.util.List;

public class WorkbookDTO {
    private Long id;
    private Long creatorId;
    private String title;
    private String description;
    private String subject;
    private String difficulty; // 상, 중, 하
    private int timeLimit;
    private int likesCount;
    private int playsCount;
    private String creatorName;
    private Timestamp createdAt;

    // 문제 만들기에서 JSON 파싱을 위해 필요한 리스트
    private List<QuestionDTO> questions;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public int getPlaysCount() { return playsCount; }
    public void setPlaysCount(int playsCount) { this.playsCount = playsCount; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
}