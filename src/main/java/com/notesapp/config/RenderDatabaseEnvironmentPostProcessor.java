package com.notesapp.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Converts Render/Heroku-style {@code postgresql://} URLs into Spring JDBC properties.
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE = "renderDatabase";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isPostgresProfile(environment)) {
            return;
        }

        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank() || databaseUrl.startsWith("jdbc:")) {
            return;
        }

        ParsedPostgresUrl parsed = ParsedPostgresUrl.parse(databaseUrl);
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", parsed.jdbcUrl());
        properties.put("spring.datasource.username", parsed.username());
        properties.put("spring.datasource.password", parsed.password());

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
    }

    private boolean isPostgresProfile(ConfigurableEnvironment environment) {
        String active = environment.getProperty("SPRING_PROFILES_ACTIVE", "sqlite");
        return active.contains("postgres");
    }

    private record ParsedPostgresUrl(String jdbcUrl, String username, String password) {

        static ParsedPostgresUrl parse(String databaseUrl) {
            String normalized = databaseUrl.replaceFirst("^postgres(ql)?://", "");
            int at = normalized.lastIndexOf('@');
            if (at < 0) {
                throw new IllegalArgumentException("Invalid DATABASE_URL: missing credentials");
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
