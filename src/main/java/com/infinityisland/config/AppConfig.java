package com.infinityisland.config;

import com.infinityisland.controller.AuthResource;
import com.infinityisland.controller.QuizResource;
import com.infinityisland.controller.UserResource;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/api")
public class AppConfig extends ResourceConfig {
    public AppConfig() {
        register(UserResource.class);
        register(AuthResource.class);
        register(QuizResource.class);
        register(CorsFilter.class);
        register(JacksonJsonProvider.class);
    }
}