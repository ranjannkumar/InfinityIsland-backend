package com.infinityisland.controller;

import com.infinityisland.dao.user.ProgressState;
import com.infinityisland.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Autowired
    UserService userService;

    /** GET /api/v1/user/daily */
    @GET
    @Path("/daily")
    public Response getDaily(@Context HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId"); // set by PinAuthFilter
        var view = userService.getDaily(userId);
        return Response.ok(new DailyResponse(view.correctCountToday, view.totalActiveMsToday, view.grandTotalCorrect))
                .build();
    }

    /** GET /api/v1/user/progress */
    @GET
    @Path("/progress")
    public Response getProgress(@Context HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        Map<String, ProgressState> progress = userService.getProgress(userId);
        return Response.ok(new ProgressResponse(progress)).build();
    }

    /** POST /api/v1/user/reset */
    @POST
    @Path("/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reset(@Context HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        userService.resetUser(userId);
        return Response.ok().build(); // Node returns 200; change to 204 if you prefer
    }

    // --- DTOs for responses ---
    public static record DailyResponse(long correctCount, long totalActiveMs, long grandTotalCorrect) {}
    public static record ProgressResponse(Map<String, ProgressState> progress) {}
}
