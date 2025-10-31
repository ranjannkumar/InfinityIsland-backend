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

    /**
     * GET /api/v1/user/daily -> { correctCount, totalActiveMs, grandTotalCorrect }
     */
    @GET
    @Path("/daily")
    public Response getDaily(@Context HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        var view = userService.getDaily(userId);
        return Response.ok(new DailyResponse(view.correctCountToday, view.totalActiveMsToday, view.grandTotalCorrect))
                .build();
    }

    /**
     * GET /api/v1/user/progress -> { progress: { "L1": {...}, ... } }
     */
    @GET
    @Path("/progress")
    public Response getProgress(@Context HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        Map<String, ProgressState> progress = userService.getProgress(userId);
        return Response.ok(new ProgressResponse(progress)).build();
    }

    /**
     * POST /api/v1/user/reset -> 200 OK
     */
    @POST
    @Path("/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reset(@Context HttpServletRequest req) {
        String userId = (String) req.getAttribute("userId");
        userService.resetUser(userId);
        return Response.ok().build();
    }

    /**
     * POST /api/v1/user/theme -> { success: true, theme } (first-set only)
     */
    @POST
    @Path("/theme")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTheme(@Context HttpServletRequest req, ThemeRequest body) {
        String userId = (String) req.getAttribute("userId");
        if (body == null || body.themeKey == null || body.themeKey.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Theme key required")).build();
        }
        try {
            String theme = userService.setThemeFirstTime(userId, body.themeKey);
            return Response.ok(Map.of("success", true, "theme", theme)).build();
        } catch (IllegalStateException ise) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Theme already set")).build();
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", iae.getMessage())).build();
        }
    }

    // --- DTOs for responses/requests ---
    public static record DailyResponse(long correctCount, long totalActiveMs, long grandTotalCorrect) {
    }

    public static record ProgressResponse(Map<String, ProgressState> progress) {
    }

    public static class ThemeRequest {
        public String themeKey;
    }
}
