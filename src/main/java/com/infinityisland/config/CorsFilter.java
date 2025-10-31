package com.infinityisland.config;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Provider
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private Set<String> allowedOrigins;

    @PostConstruct
    void init() {
        allowedOrigins = new HashSet<>(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://infinity-island-frontend.vercel.app",
                "https://infinity-island-frontend-poonam-anands-projects.vercel.app",
                "https://infinity-island-frontend-git-main-poonam-anands-projects.vercel.app",
                "https://infinity-island-frontend-4ypijt4j1-poonam-anands-projects.vercel.app"
        ));
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(jakarta.ws.rs.core.Response.ok().build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        String origin = requestContext.getHeaderString("Origin");

        if (origin != null && (allowedOrigins.contains(origin) || allowedOrigins.contains("*"))) {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().putSingle("Vary", "Origin");
            responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-pin");
            responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            responseContext.getHeaders().putSingle("Access-Control-Max-Age", "86400");
        }
    }
}
