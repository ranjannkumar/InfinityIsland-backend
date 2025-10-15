package com.infinityisland.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("generated_questions")
public class GeneratedQuestion {
    @Id
    private String id;
    private String operation;
    private String level;
    private String beltOrDegree;
    private Integer a;
    private Integer b;
    private String question;
    private Integer correctAnswer;
    private List<Integer> choices;
    private String source;

    public GeneratedQuestion() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getA() {
        return a;
    }

    public void setA(Integer a) {
        this.a = a;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(Integer correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<Integer> getChoices() {
        return choices;
    }

    public void setChoices(List<Integer> choices) {
        this.choices = choices;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
