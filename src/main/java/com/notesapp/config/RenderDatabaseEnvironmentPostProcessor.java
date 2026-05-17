package com.notesapp.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Converts Render {@code postgresql://} DATABASE_URL into Spring JDBC properties (highest priority).
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE = "renderDatabase";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("DATABASE_INTERNAL_URL"));

        boolean postgresProfile = isPostgresProfile(environment);
        System.out.println("[notes-api] SPRING_PROFILES_ACTIVE="
                + environment.getProperty("SPRING_PROFILES_ACTIVE", "?")
                + ", postgresProfile="
                + postgresProfile
                + ", DATABASE_URL set="
                + (databaseUrl != null && !databaseUrl.isBlank())
                + ", DB_HOST="
                + environment.getProperty("DB_HOST", "<not set>"));

        if (!PostgresUrlParser.isPostgresScheme(databaseUrl)) {
            if (postgresProfile && isBlank(environment.getProperty("DB_HOST"))) {
                System.err.println(
                        "[notes-api] ERROR: postgres profile active but DATABASE_URL and DB_HOST are missing. "
                                + "On Render: notes-api → Environment → Link Database → notes-db");
            }
            return;
        }

        PostgresUrlParser.ParsedJdbc parsed = PostgresUrlParser.parse(databaseUrl);
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", parsed.jdbcUrl());
        properties.put("spring.datasource.username", parsed.username());
        properties.put("spring.datasource.password", parsed.password());
        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        properties.put("SPRING_PROFILES_ACTIVE", "postgres");

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
        System.out.println("[notes-api] Using JDBC URL from DATABASE_URL (host parsed from connection string)");
    }

    private static boolean isPostgresProfile(ConfigurableEnvironment environment) {
        String active = firstNonBlank(
                environment.getProperty("SPRING_PROFILES_ACTIVE"),
                environment.getProperty("spring.profiles.active"));
        return active != null && active.contains("postgres");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
