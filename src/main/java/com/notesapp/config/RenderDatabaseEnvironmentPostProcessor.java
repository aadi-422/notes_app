package com.notesapp.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Configures PostgreSQL for Render: prefers internal {@code DB_HOST} over external {@code DATABASE_URL}.
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE = "renderDatabase";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean postgresProfile = isPostgresProfile(environment);
        String dbHost = environment.getProperty("DB_HOST");
        String databaseUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("DATABASE_INTERNAL_URL"));

        System.out.println("[notes-api] SPRING_PROFILES_ACTIVE="
                + environment.getProperty("SPRING_PROFILES_ACTIVE", "?")
                + ", postgresProfile="
                + postgresProfile
                + ", DB_HOST="
                + (isBlank(dbHost) ? "<not set>" : dbHost)
                + ", DATABASE_URL set="
                + (databaseUrl != null && !databaseUrl.isBlank()));

        if (!isBlank(dbHost)) {
            applyInternalHostConfig(environment, dbHost);
            return;
        }

        if (PostgresUrlParser.isPostgresScheme(databaseUrl)) {
            applyDatabaseUrlConfig(environment, databaseUrl);
            return;
        }

        if (postgresProfile) {
            System.err.println(
                    "[notes-api] ERROR: postgres profile but DB_HOST / DATABASE_URL missing. "
                            + "Render: notes-api → Environment → Link notes-db (use internal host fields).");
        }
    }

    /** Internal Render hostname (from blueprint {@code property: host}) — reliable from web services. */
    private void applyInternalHostConfig(ConfigurableEnvironment environment, String dbHost) {
        String port = firstNonBlank(environment.getProperty("DB_PORT"), "5432");
        String database = firstNonBlank(environment.getProperty("DB_NAME"), "notes_db");
        String username = firstNonBlank(environment.getProperty("DB_USER"), "notes_user");
        String password = firstNonBlank(environment.getProperty("DB_PASSWORD"), "");

        String jdbcUrl = PostgresUrlParser.buildJdbcUrl(dbHost, port, database);
        Map<String, Object> properties = datasourceProperties(jdbcUrl, username, password);

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
        System.out.println("[notes-api] Using internal PostgreSQL at " + dbHost + ":" + port + "/" + database);
    }

    /** Fallback when only DATABASE_URL is set (e.g. manual env). May be external — prefer DB_HOST on Render. */
    private void applyDatabaseUrlConfig(ConfigurableEnvironment environment, String databaseUrl) {
        if (databaseUrl.contains(".render.com")) {
            System.err.println(
                    "[notes-api] WARN: DATABASE_URL looks like an external Render URL. "
                            + "Remove DATABASE_URL and use DB_HOST from 'Add from database' instead.");
        }

        PostgresUrlParser.ParsedJdbc parsed = PostgresUrlParser.parse(databaseUrl);
        Map<String, Object> properties = datasourceProperties(parsed.jdbcUrl(), parsed.username(), parsed.password());

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
        System.out.println("[notes-api] Using JDBC URL parsed from DATABASE_URL");
    }

    private static Map<String, Object> datasourceProperties(String jdbcUrl, String username, String password) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", jdbcUrl);
        properties.put("spring.datasource.username", username);
        properties.put("spring.datasource.password", password);
        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        properties.put("SPRING_PROFILES_ACTIVE", "postgres");
        return properties;
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
