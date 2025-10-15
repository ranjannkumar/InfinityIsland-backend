package com.infinityisland.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("attempts")
public class Attempt {
    @Id
    private String id;
    private String quizRunId;
    private String questionId;
    private Integer userAnswer;
    private Boolean correct;
    private Long responseMs;
    private String reason;
    private Boolean triggeredPractice;
    private Boolean practiceCompleted;

    public Attempt() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuizRunId() {
        return quizRunId;
    }

    public void setQuizRunId(String quizRunId) {
        this.quizRunId = quizRunId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public Integer getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(Integer userAnswer) {
        this.userAnswer = userAnswer;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Long getResponseMs() {
        return responseMs;
    }

    public void setResponseMs(Long responseMs) {
        this.responseMs = responseMs;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getTriggeredPractice() {
        return triggeredPractice;
    }

    public void setTriggeredPractice(Boolean triggeredPractice) {
        this.triggeredPractice = triggeredPractice;
    }

    public Boolean getPracticeCompleted() {
        return practiceCompleted;
    }

    public void setPracticeCompleted(Boolean practiceCompleted) {
        this.practiceCompleted = practiceCompleted;
    }
}
