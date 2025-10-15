package com.infinityisland.controller;

import com.infinityisland.dao.resource.AuthUser;
import com.infinityisland.dao.resource.LoginPinRequest;
import com.infinityisland.dao.resource.LoginPinResponse;
import com.infinityisland.dao.user.BeltStatus;
import com.infinityisland.dao.user.BlackProgress;
import com.infinityisland.dao.user.ProgressState;
import com.infinityisland.dao.user.User;
import com.infinityisland.repositories.UserRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UserRepository userRepository;

    public AuthResource(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * POST /api/v1/auth/login-pin
     */
    @POST
    @Path("/login-pin")
    public Response loginPin(LoginPinRequest body) {
        // ---- validate
        if (body == null || body.getPin() == null || body.getPin().isBlank()) {
            return badRequest("pin is required");
        }
        String pin = body.getPin().trim();
        String name = (body.getPin() != null && !body.getPin().isBlank()) ? body.getPin().trim() : "Player";

        // ---- find or create user by PIN
        User user = userRepository.findByPin(pin).map(u -> {
            // Optional: update name on first login only
            if ((u.getName() == null || u.getName().isBlank()) && name != null && !name.isBlank()) {
                u.setName(name);
                userRepository.save(u);
            }
            return u;
        }).orElseGet(() -> {
            User u = new User();
            u.setPin(pin);
            u.setName(name);
            u.setTheme("animals");

            // default L1 progress with white unlocked
            Map<String, ProgressState> progress = new HashMap<>();
            progress.put("L1", defaultL1Progress());
            u.setProgress(progress);

            // empty daily stats map
            u.setDailyStats(new HashMap<>());

            return userRepository.save(u);
        });

        // ---- build response { token, user: { id, name, progress, dailyStats } }
        AuthUser userDto = new AuthUser(
                user.getId(),
                user.getName(),
                user.getProgress() != null ? user.getProgress() : Map.of(),
                user.getDailyStats() != null ? user.getDailyStats() : Map.of()
        );
        LoginPinResponse resp = new LoginPinResponse(user.getPin(), userDto);

        return Response.ok(resp).build();
    }

    // ---------------- helpers ----------------

    private ProgressState defaultL1Progress() {
        ProgressState p = new ProgressState();
        p.setLevel(1);
        p.setUnlocked(true);
        p.setComplete(false);

        BeltStatus white = new BeltStatus();
        white.setUnlocked(true);
        p.setWhite(white);

        p.setYellow(new BeltStatus()); // locked
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
        // matches frontend error handling: data.error?.message
        Map<String, Object> err = Map.of("error", Map.of("message", message));
        return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
    }
}