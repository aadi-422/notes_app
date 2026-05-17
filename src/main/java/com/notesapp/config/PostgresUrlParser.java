package com.notesapp.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class PostgresUrlParser {

    private PostgresUrlParser() {}

    public record ParsedJdbc(String jdbcUrl, String username, String password) {}

    public static boolean isPostgresScheme(String url) {
        return url != null
                && (url.startsWith("postgresql://") || url.startsWith("postgres://"));
    }

    public static ParsedJdbc parse(String databaseUrl) {
        String normalized = databaseUrl.replaceFirst("^postgres(ql)?://", "");
        int at = normalized.lastIndexOf('@');
        if (at < 0) {
            throw new IllegalArgumentException("Invalid postgres URL: missing user:pass@host");
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

        return new ParsedJdbc(jdbcUrl, username, password);
    }

    public static String buildJdbcUrl(String host, String port, String database) {
        return "jdbc:postgresql://"
                + host
                + ":"
                + port
                + "/"
                + database
                + "?sslmode=require&connectTimeout=60&socketTimeout=60";
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
