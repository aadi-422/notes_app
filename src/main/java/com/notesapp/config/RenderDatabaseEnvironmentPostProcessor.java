package com.notesapp.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Converts Render/Heroku {@code postgresql://} DATABASE_URL into Spring JDBC datasource properties.
 * Runs whenever DATABASE_URL is a non-JDBC postgres URL (no profile check — profiles may not be active yet).
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE = "renderDatabase";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("DATABASE_INTERNAL_URL"));

        if (databaseUrl == null || databaseUrl.isBlank() || databaseUrl.startsWith("jdbc:")) {
            return;
        }

        if (!databaseUrl.startsWith("postgresql://") && !databaseUrl.startsWith("postgres://")) {
            return;
        }

        try {
            ParsedPostgresUrl parsed = ParsedPostgresUrl.parse(databaseUrl);
            Map<String, Object> properties = new HashMap<>();
            properties.put("spring.datasource.url", parsed.jdbcUrl());
            properties.put("spring.datasource.username", parsed.username());
            properties.put("spring.datasource.password", parsed.password());
            properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            properties.put("SPRING_PROFILES_ACTIVE", "postgres");

            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to parse DATABASE_URL for PostgreSQL: " + ex.getMessage(), ex);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private record ParsedPostgresUrl(String jdbcUrl, String username, String password) {

        static ParsedPostgresUrl parse(String databaseUrl) {
            String normalized = databaseUrl.replaceFirst("^postgres(ql)?://", "");
            int at = normalized.lastIndexOf('@');
            if (at < 0) {
                throw new IllegalArgumentException("missing credentials (expected user:pass@host/db)");
            }

            String userInfo = normalized.substring(0, at);
            String hostAndDb = normalized.substring(at + 1);

            String username;
            String password = "";
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                username = decode(userInfo.substring(0, colon));
                password = decode(userInfo.substring(colon + 1));
            } else {
                username = decode(userInfo);
            }

            String jdbcUrl = "jdbc:postgresql://" + hostAndDb;
            if (!jdbcUrl.contains("sslmode=")) {
                jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
            }

            return new ParsedPostgresUrl(jdbcUrl, username, password);
        }

        private static String decode(String value) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }
    }
}
