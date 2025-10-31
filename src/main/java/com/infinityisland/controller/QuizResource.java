package com.infinityisland.controller;

import com.infinityisland.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;

@Component
@Path("/quiz")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QuizResource {

    private final QuizService quizService;

    public QuizResource(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * POST /api/v1/quiz/prepare
     * Request: { level, beltOrDegree, operation }
     * Response: { quizRunId, practice: QuestionDto[] }
     */
    @POST
    @Path("/prepare")
    public Response prepare(@Context HttpServletRequest req, QuizDtos.PrepareRequest body) {
        String userId = (String) req.getAttribute("userId");
        var resp = quizService.prepare(userId, body);
        return Response.ok(resp).build();
    }

    /**
     * POST /api/v1/quiz/start
     * Request: { quizRunId }
     * Response: { quizRunId, questions: QuestionDto[] } // full prefetch (Node parity)
     */
    @POST
    @Path("/start")
    public Response start(@Context HttpServletRequest req, QuizDtos.StartRequest body) {
        String userId = (String) req.getAttribute("userId");
        var resp = quizService.start(userId, body);
        return Response.ok(resp).build();
    }

    /**
     * POST /api/v1/quiz/answer
     * Request: { quizRunId, questionId, answer, responseMs, level, beltOrDegree }
     * Response: AnswerOrPracticeResponse
     */
    @POST
    @Path("/answer")
    public Response answer(@Context HttpServletRequest req, QuizDtos.AnswerRequest body) {
        String userId = (String) req.getAttribute("userId");
        var resp = quizService.answer(userId, body);
        return Response.ok(resp).build();
    }

    /**
     * POST /api/v1/quiz/inactivity
     * Request: { quizRunId, questionId }
     * Response: AnswerOrPracticeResponse
     */
    @POST
    @Path("/inactivity")
    public Response inactivity(@Context HttpServletRequest req, QuizDtos.InactivityRequest body) {
        String userId = (String) req.getAttribute("userId");
        var resp = quizService.inactivity(userId, body);
        return Response.ok(resp).build();
    }

    /**
     * POST /api/v1/quiz/practice/answer
     * Request: { quizRunId, questionId, answer }
     * Response: AnswerOrPracticeResponse
     */
    @POST
    @Path("/practice/answer")
    public Response practiceAnswer(@Context HttpServletRequest req, QuizDtos.PracticeAnswerRequest body) {
        String userId = (String) req.getAttribute("userId");
        var resp = quizService.practiceAnswer(userId, body);
        return Response.ok(resp).build();
    }

    /**
     * POST /api/v1/quiz/complete
     * Request: { quizRunId }
     * Response: { completed: true, totalCorrect, dailyStats? } or 200 OK depending on service
     */
    @POST
    @Path("/complete")
    public Response complete(@Context HttpServletRequest req, QuizDtos.CompleteRequest body) {
        String userId = (String) req.getAttribute("userId");
        var resp = quizService.complete(userId, body);
        return Response.ok(resp).build();
    }
}