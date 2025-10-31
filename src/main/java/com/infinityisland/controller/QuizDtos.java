package com.infinityisland.controller;

import java.util.List;

public final class QuizDtos {
    // ---- requests ----
    public record PrepareRequest(String level, String beltOrDegree, String operation) {
    }

    public record StartRequest(String quizRunId) {
    }

    public record AnswerRequest(String quizRunId, String questionId, Integer answer,
                                Long responseMs, String level, String beltOrDegree) {
    }

    public record InactivityRequest(String quizRunId, String questionId) {
    }

    public record PracticeAnswerRequest(String quizRunId, String questionId, Integer answer) {
    }

    public record CompleteRequest(String quizRunId) {
    }

    // ---- responses ----

    /**
     * prepare returns the run id plus a prefetch list for practice (in-memory).
     */
    public record PrepareResponse(String quizRunId, List<QuestionDto> practice) {
    }

    /**
     * start returns the full prefetched question list for the run (Node parity).
     */
    public record StartResponse(String quizRunId, List<QuestionDto> questions) {
    }

    /**
     * Unified payload for answer/practice/inactivity paths.
     * - completed: true when the run is complete
     * - resumed: true when resuming the main quiz after a correct practice
     * - next: next main question to show (if any)
     * - practice: practice item to show (if any)
     * - totalCorrect: running tally of correct answers in this run
     * - dailyStats: today's roll-up and grand total
     */
    public record AnswerOrPracticeResponse(
            Boolean completed,
            Boolean resumed,
            QuestionDto next,
            QuestionDto practice,
            Integer totalCorrect,
            DailyStatsDto dailyStats
    ) {
    }

    /**
     * Today's stats and grand total (used on certain transitions).
     */
    public record DailyStatsDto(
            Long correctCount,
            Long totalActiveMs,
            Long grandTotalCorrect
    ) {
    }

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
    ) {
    }
}