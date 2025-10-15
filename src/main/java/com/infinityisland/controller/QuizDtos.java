package com.infinityisland.controller;

import java.util.List;

public final class QuizDtos {
    // ---- requests ----
    public record PrepareRequest(String level, String beltOrDegree, String operation) {}
    public record StartRequest(String quizRunId) {}
    public record AnswerRequest(String quizRunId, String questionId, Integer answer,
                                Long responseMs, String level, String beltOrDegree) {}
    public record InactivityRequest(String quizRunId, String questionId) {}
    public record PracticeAnswerRequest(String quizRunId, String questionId, Integer answer) {}
    public record CompleteRequest(String quizRunId) {}

    // ---- responses ----
    public record PrepareResponse(String quizRunId, List<QuestionDto> practice) {}
    public record StartResponse(QuestionDto question) {}

    public record DailyStatsDto(long correctCount, long totalActiveMs, long grandTotal) {}

    /**
     * Unified response used by /answer, /inactivity, /practice/answer, /complete.
     * Use Boolean (nullable) so absent fields are omitted in JSON.
     */
    public record AnswerOrPracticeResponse(
            Boolean completed,          // true when quiz ends
            Boolean resume,             // true when resuming after practice
            QuestionDto next,           // next main question
            QuestionDto practice,       // practice question
            Integer sessionCorrectCount,
            DailyStatsDto dailyStats
    ) {}

    public record QuestionDto(
            String id,
            String operation,
            String level,
            String beltOrDegree,
            int a,
            int b,
            String question,
            Integer correctAnswer,
            List<Integer> choices
    ) {}
}
