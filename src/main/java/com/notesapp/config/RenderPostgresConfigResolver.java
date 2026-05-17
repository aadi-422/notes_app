package com.notesapp.config;

import org.springframework.core.env.Environment;

/** Resolves PostgreSQL JDBC settings from Render environment variables. */
public final class RenderPostgresConfigResolver {

    private RenderPostgresConfigResolver() {}

    public record PostgresConfig(String jdbcUrl, String username, String password, String source) {}

    public static PostgresConfig resolve(Environment env) {
        String dbHost = env.getProperty("DB_HOST");
        String databaseUrl = firstNonBlank(
                env.getProperty("DATABASE_URL"),
                env.getProperty("DATABASE_INTERNAL_URL"));

        if (!isBlank(dbHost)) {
            String port = firstNonBlank(env.getProperty("DB_PORT"), "5432");
            String database = firstNonBlank(env.getProperty("DB_NAME"), "notes_db");
            String username = firstNonBlank(env.getProperty("DB_USER"), "notes_user");
            String password = firstNonBlank(env.getProperty("DB_PASSWORD"), "");
            String jdbcUrl = PostgresUrlParser.buildJdbcUrl(dbHost, port, database);
            return new PostgresConfig(jdbcUrl, username, password, "DB_HOST=" + dbHost);
        }

        if (PostgresUrlParser.isPostgresScheme(databaseUrl)) {
            PostgresUrlParser.ParsedJdbc parsed = PostgresUrlParser.parse(databaseUrl);
            return new PostgresConfig(parsed.jdbcUrl(), parsed.username(), parsed.password(), "DATABASE_URL");
        }

        throw new IllegalStateException(
                """
                PostgreSQL is not configured on notes-api.

                On Render:
                  1. notes-db → Connect → select notes-api → Connect
                  OR
                  2. notes-api → Environment → Add from database → notes-db → Save all fields

                Required: DATABASE_URL or DB_HOST (and DB_USER, DB_PASSWORD, DB_NAME, DB_PORT).
                """);
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
