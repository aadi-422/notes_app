package com.notesapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class RenderDatabaseEnvironmentPostProcessorTest {

    private final RenderDatabaseEnvironmentPostProcessor processor =
            new RenderDatabaseEnvironmentPostProcessor();

    @Test
    void prefersDbHostOverDatabaseUrl() {
        StandardEnvironment env = environmentWith(Map.of(
                "SPRING_PROFILES_ACTIVE", "postgres",
                "DB_HOST", "dpg-internal",
                "DB_PORT", "5432",
                "DB_NAME", "notes_db",
                "DB_USER", "notes_user",
                "DB_PASSWORD", "secret",
                "DATABASE_URL",
                        "postgresql://notes_user:pass@dpg-external.oregon-postgres.render.com/notes_db"));

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url"))
                .contains("dpg-internal")
                .doesNotContain("oregon-postgres.render.com");
    }

    @Test
    void failsFastWhenPostgresProfileWithoutDatabaseConfig() {
        StandardEnvironment env = environmentWith(Map.of("SPRING_PROFILES_ACTIVE", "postgres"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> processor.postProcessEnvironment(env, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PostgreSQL is not configured");
    }

    @Test
    void convertsDatabaseUrlWhenDbHostMissing() {
        StandardEnvironment env = environmentWith(Map.of(
                "SPRING_PROFILES_ACTIVE", "postgres",
                "DATABASE_URL",
                "postgresql://notes_user:secret%40pass@dpg-example.oregon-postgres.render.com/notes_db"));

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url"))
                .startsWith("jdbc:postgresql://dpg-example.oregon-postgres.render.com/notes_db");
        assertThat(env.getProperty("spring.datasource.url")).contains("sslmode=require");
        assertThat(env.getProperty("spring.datasource.username")).isEqualTo("notes_user");
        assertThat(env.getProperty("spring.datasource.password")).isEqualTo("secret@pass");
    }

    @Test
    void parserBuildsJdbcFromHostParts() {
        String jdbc = PostgresUrlParser.buildJdbcUrl("dpg-internal", "5432", "notes_db");
        assertThat(jdbc)
                .startsWith("jdbc:postgresql://dpg-internal:5432/notes_db")
                .contains("sslmode=require");
    }

    private static StandardEnvironment environmentWith(Map<String, Object> properties) {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("test", new HashMap<>(properties)));
        return env;
    }
}
