package com.infinityisland.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document("quiz_runs")
public class QuizRun {
    @Id
    private String id;
    private String userId;
    private String operation;
    private String level;
    private String beltOrDegree;
    private String status;
    private Integer currentIndex;
    private Instant createdAt;
    private Long totalActiveMs;
    private Integer correct;
    private Integer wrong;
    private List<String> itemIds;

    public QuizRun() {
        createdAt = Instant.now();
        itemIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getBeltOrDegree() {
        return beltOrDegree;
    }

    public void setBeltOrDegree(String beltOrDegree) {
        this.beltOrDegree = beltOrDegree;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(Integer currentIndex) {
        this.currentIndex = currentIndex;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getTotalActiveMs() {
        return totalActiveMs;
    }

    public void setTotalActiveMs(Long totalActiveMs) {
        this.totalActiveMs = totalActiveMs;
    }

    public Integer getCorrect() {
        return correct;
    }

    public void setCorrect(Integer correct) {
        this.correct = correct;
    }

    public Integer getWrong() {
        return wrong;
    }

    public void setWrong(Integer wrong) {
        this.wrong = wrong;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }
}
