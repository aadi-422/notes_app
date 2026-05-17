package com.notesapp.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fail fast before Spring context if postgres profile is active but DB env vars are missing.
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String active = environment.getProperty("SPRING_PROFILES_ACTIVE", "");
        if (!active.contains("postgres")) {
            return;
        }

        System.out.println("[notes-api] postgres profile | DB_HOST="
                + environment.getProperty("DB_HOST", "<not set>")
                + " | DATABASE_URL="
                + (environment.getProperty("DATABASE_URL") != null ? "set" : "<not set>"));

        try {
            RenderPostgresConfigResolver.resolve(environment);
        } catch (IllegalStateException ex) {
            throw ex;
        }
    }
}
