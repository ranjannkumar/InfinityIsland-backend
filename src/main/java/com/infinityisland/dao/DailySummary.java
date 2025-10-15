package com.infinityisland.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("daily_summaries")
public class DailySummary {
    @Id
    private String id;
    private String userId;
    private String date;
    private Long correctCount;
    private Long totalActiveMs;
    private Boolean reportSentMarker;

    public DailySummary() {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Long correctCount) {
        this.correctCount = correctCount;
    }

    public Long getTotalActiveMs() {
        return totalActiveMs;
    }

    public void setTotalActiveMs(Long totalActiveMs) {
        this.totalActiveMs = totalActiveMs;
    }

    public Boolean getReportSentMarker() {
        return reportSentMarker;
    }

    public void setReportSentMarker(Boolean reportSentMarker) {
        this.reportSentMarker = reportSentMarker;
    }
}
