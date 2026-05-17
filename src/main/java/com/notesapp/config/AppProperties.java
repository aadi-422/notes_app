package com.notesapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, About about) {

    public record Jwt(String secret, long expirationMs) {}

    public record About(String name, String email) {}
}
