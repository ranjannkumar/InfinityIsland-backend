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

    @POST @Path("/prepare")
    public Response prepare(@Context HttpServletRequest req, QuizDtos.PrepareRequest body) {
        String userId = (String) req.getAttribute("userId");
        return Response.ok(quizService.prepare(userId, body)).build();
    }

    @POST @Path("/start")
    public Response start(@Context HttpServletRequest req, QuizDtos.StartRequest body) {
        String userId = (String) req.getAttribute("userId");
        return Response.ok(quizService.start(userId, body)).build();
    }

    @POST @Path("/answer")
    public Response answer(@Context HttpServletRequest req, QuizDtos.AnswerRequest body) {
        String userId = (String) req.getAttribute("userId");
        return Response.ok(quizService.answer(userId, body)).build();
    }

    @POST @Path("/inactivity")
    public Response inactivity(@Context HttpServletRequest req, QuizDtos.InactivityRequest body) {
        String userId = (String) req.getAttribute("userId");
        return Response.ok(quizService.inactivity(userId, body)).build();
    }

    @POST @Path("/practice/answer")
    public Response practiceAnswer(@Context HttpServletRequest req, QuizDtos.PracticeAnswerRequest body) {
        String userId = (String) req.getAttribute("userId");
        return Response.ok(quizService.practiceAnswer(userId, body)).build();
    }

    @POST @Path("/complete")
    public Response complete(@Context HttpServletRequest req, QuizDtos.CompleteRequest body) {
        String userId = (String) req.getAttribute("userId");
        return Response.ok(quizService.complete(userId, body)).build();
    }
}
