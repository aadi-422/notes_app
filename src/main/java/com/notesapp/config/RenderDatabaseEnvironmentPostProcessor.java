package com.notesapp.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Configures PostgreSQL for Render from DB_* (internal host) or DATABASE_URL (linked DB).
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE = "renderDatabase";

    private static final String RENDER_HELP =
            """
            PostgreSQL is not configured on this service.

            Fix on Render (pick one):
            A) notes-db → Connect → select service "notes-api" → Connect
            B) notes-api → Environment → Add from database → notes-db → add all fields → Save
            C) Dashboard → Blueprints → your blueprint → Manual Sync (applies render.yaml env vars)

            You need at least DATABASE_URL or DB_HOST in notes-api Environment.
            """;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isPostgresProfile(environment)) {
            return;
        }

        String dbHost = environment.getProperty("DB_HOST");
        String databaseUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("DATABASE_INTERNAL_URL"));

        System.out.println("[notes-api] postgres profile | DB_HOST="
                + (isBlank(dbHost) ? "<not set>" : dbHost)
                + " | DATABASE_URL="
                + (isBlank(databaseUrl) ? "<not set>" : "set"));

        if (!isBlank(dbHost)) {
            applyInternalHostConfig(environment, dbHost);
            return;
        }

        if (PostgresUrlParser.isPostgresScheme(databaseUrl)) {
            applyDatabaseUrlConfig(environment, databaseUrl);
            return;
        }

        throw new IllegalStateException(RENDER_HELP);
    }

    private void applyInternalHostConfig(ConfigurableEnvironment environment, String dbHost) {
        String port = firstNonBlank(environment.getProperty("DB_PORT"), "5432");
        String database = firstNonBlank(environment.getProperty("DB_NAME"), "notes_db");
        String username = firstNonBlank(environment.getProperty("DB_USER"), "notes_user");
        String password = firstNonBlank(environment.getProperty("DB_PASSWORD"), "");

        String jdbcUrl = PostgresUrlParser.buildJdbcUrl(dbHost, port, database);
        applyDatasource(environment, jdbcUrl, username, password);
        System.out.println("[notes-api] Using DB_HOST at " + dbHost + ":" + port + "/" + database);
    }

    private void applyDatabaseUrlConfig(ConfigurableEnvironment environment, String databaseUrl) {
        PostgresUrlParser.ParsedJdbc parsed = PostgresUrlParser.parse(databaseUrl);
        if (parsed.jdbcUrl().contains(".render.com")) {
            System.out.println(
                    "[notes-api] NOTE: DATABASE_URL uses external hostname; if connection fails, "
                            + "use notes-db → Connect → notes-api for internal URL, or set DB_HOST.");
        }
        applyDatasource(environment, parsed.jdbcUrl(), parsed.username(), parsed.password());
        System.out.println("[notes-api] Using DATABASE_URL (parsed to JDBC)");
    }

    private void applyDatasource(
            ConfigurableEnvironment environment, String jdbcUrl, String username, String password) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", jdbcUrl);
        properties.put("spring.datasource.username", username);
        properties.put("spring.datasource.password", password);
        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
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
