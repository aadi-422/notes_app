package com.notesapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

class RenderPostgresConfigResolverTest {

    @Test
    void prefersDbHostOverDatabaseUrl() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DB_HOST", "dpg-internal");
        env.setProperty("DB_PORT", "5432");
        env.setProperty("DB_NAME", "notes_db");
        env.setProperty("DB_USER", "notes_user");
        env.setProperty("DB_PASSWORD", "secret");
        env.setProperty(
                "DATABASE_URL",
                "postgresql://notes_user:pass@dpg-external.oregon-postgres.render.com/notes_db");

        var config = RenderPostgresConfigResolver.resolve(env);

        assertThat(config.jdbcUrl()).contains("dpg-internal").doesNotContain("oregon-postgres.render.com");
        assertThat(config.source()).contains("DB_HOST");
    }

    @Test
    void parsesDatabaseUrlWhenDbHostMissing() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty(
                "DATABASE_URL",
                "postgresql://notes_user:secret%40pass@dpg-example.oregon-postgres.render.com/notes_db");

        var config = RenderPostgresConfigResolver.resolve(env);

        assertThat(config.jdbcUrl()).contains("dpg-example.oregon-postgres.render.com");
        assertThat(config.username()).isEqualTo("notes_user");
        assertThat(config.password()).isEqualTo("secret@pass");
    }

    @Test
    void failsWhenNothingConfigured() {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("test", new HashMap<>()));

        assertThatThrownBy(() -> RenderPostgresConfigResolver.resolve(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PostgreSQL is not configured");
    }
}
