package com.infinityisland.controller;

import com.infinityisland.dao.resource.AuthUser;
import com.infinityisland.dao.resource.LoginPinRequest;
import com.infinityisland.dao.resource.LoginPinResponse;
import com.infinityisland.dao.user.BeltStatus;
import com.infinityisland.dao.user.BlackProgress;
import com.infinityisland.dao.user.DailyStats;
import com.infinityisland.dao.user.ProgressState;
import com.infinityisland.dao.user.User;
import com.infinityisland.repositories.UserRepository;
import com.infinityisland.service.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UserRepository userRepository;
    private final UserService userService;

    public AuthResource(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * POST /api/v1/auth/login-pin
     * Request: { pin, name? }
     * Response: { token, user }
     * user includes: theme, progress, dailyStats(today), grandTotal, currentStreak, lastLoginDate
     */
    @POST
    @Path("/login-pin")
    public Response loginPin(LoginPinRequest body) {
        if (body == null || !StringUtils.hasText(body.getPin())) {
            return badRequest("PIN required");
        }
        final String pin = body.getPin().trim();
        final String inputName = StringUtils.hasText(body.getName()) ? body.getName().trim() : null;

        Optional<User> existing = userRepository.findByPin(pin);
        User user;
        if (existing.isPresent()) {
            user = existing.get();
            if (StringUtils.hasText(inputName)
                    && StringUtils.hasText(user.getName())
                    && !user.getName().equalsIgnoreCase(inputName)) {
                Map<String, Object> err = Map.of(
                        "error", Map.of(
                                "message", "Pin already exists, please enter correct name.",
                                "code", "INCORRECT_NAME"
                        )
                );
                return Response.status(Response.Status.UNAUTHORIZED).entity(err).build();
            }
            if (!StringUtils.hasText(user.getName()) && StringUtils.hasText(inputName)) {
                user.setName(inputName);
                userRepository.save(user);
            }
        } else {
            if (!StringUtils.hasText(inputName)) {
                return badRequest("Name required for new user signup.");
            }
            user = new User();
            user.setPin(pin);
            user.setName(inputName);
            Map<String, ProgressState> progress = new HashMap<>();
            progress.put("L1", defaultL1Progress());
            user.setProgress(progress);
            user.setDailyStats(new HashMap<>());
            user = userRepository.save(user);
        }

        // Update streak + lastLoginDate in Pacific time
        int newStreak = userService.updateLoginStreakPacific(user);

        // Today's summary + grand total
        var view = userService.getDaily(user.getId());
        DailyStats today = new DailyStats();
        today.setCorrectCount(view.correctCountToday);
        today.setTotalActiveMs(view.totalActiveMsToday);

        AuthUser dto = new AuthUser(
                user.getId(),
                user.getName(),
                user.getTheme(),
                user.getProgress() != null ? user.getProgress() : Map.of(),
                today,
                view.grandTotalCorrect,
                newStreak,
                user.getLastLoginDate()
        );
        return Response.ok(new LoginPinResponse(user.getPin(), dto)).build();
    }

    private ProgressState defaultL1Progress() {
        ProgressState p = new ProgressState();
        p.setLevel(1);
        p.setUnlocked(true);
        p.setComplete(false);

        BeltStatus white = new BeltStatus();
        white.setUnlocked(true);
        p.setWhite(white);
        p.setYellow(new BeltStatus());
        p.setGreen(new BeltStatus());
        p.setBlue(new BeltStatus());
        p.setRed(new BeltStatus());
        p.setBrown(new BeltStatus());

        BlackProgress bp = new BlackProgress();
        bp.setUnlocked(false);
        bp.setCompletedDegrees(new ArrayList<>());
        p.setBlack(bp);
        return p;
    }

    private Response badRequest(String message) {
        Map<String, Object> err = Map.of("error", Map.of("message", message));
        return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
    }
}