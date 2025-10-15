package com.infinityisland.service;

import com.infinityisland.dao.user.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    /** Node semantics: log in with PIN; create user if missing; token == PIN. */
    public AuthResult loginWithPin(String pin, String name) {
        User u = userService.loginOrCreateByPin(pin, name);
        return new AuthResult(u.getId(), u.getName(), u.getPin()); // token is the PIN
    }

    public record AuthResult(String userId, String name, String token) {}
}
