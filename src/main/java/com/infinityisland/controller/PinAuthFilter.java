package com.infinityisland.controller;

import com.infinityisland.dao.user.User;
import com.infinityisland.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class PinAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    // Change to "/api/v1" if that's your prefix (or override in application.yml)
    @Value("${app.api-prefix:/api}")
    private String apiPrefix;

    public PinAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Only filter /<prefix>/user/** and /<prefix>/quiz/** */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String p = request.getRequestURI();
        return !(p.startsWith(apiPrefix + "/user") || p.startsWith(apiPrefix + "/quiz"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Header is case-insensitive; curl example: -H "x-pin: 1234"
        String pin = request.getHeader("x-pin");
        if (pin == null || pin.isBlank()) {
            unauthorized(response, "Missing x-pin header");
            return;
        }

        Optional<User> userOpt = userRepository.findByPin(pin.trim());
        if (userOpt.isEmpty()) {
            unauthorized(response, "Invalid PIN");
            return;
        }

        // Make userId available to controllers/resources
        request.setAttribute("userId", userOpt.get().getId());

        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse res, String msg) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"" + msg.replace("\"", "\\\"") + "\"}");
    }
}