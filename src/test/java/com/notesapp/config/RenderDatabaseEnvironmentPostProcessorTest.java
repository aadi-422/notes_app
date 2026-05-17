package com.notesapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.StandardEnvironment;

class RenderDatabaseEnvironmentPostProcessorTest {

    private final RenderDatabaseEnvironmentPostProcessor processor =
            new RenderDatabaseEnvironmentPostProcessor();

    @Test
    void convertsRenderPostgresUrlToJdbcWithoutActiveProfile() {
        StandardEnvironment env = new StandardEnvironment();
        env.getSystemProperties()
                .put(
                        "DATABASE_URL",
                        "postgresql://notes_user:secret%40pass@dpg-example.oregon-postgres.render.com/notes_db");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url"))
                .startsWith("jdbc:postgresql://dpg-example.oregon-postgres.render.com/notes_db");
        assertThat(env.getProperty("spring.datasource.url")).contains("sslmode=require");
        assertThat(env.getProperty("spring.datasource.username")).isEqualTo("notes_user");
        assertThat(env.getProperty("spring.datasource.password")).isEqualTo("secret@pass");
        assertThat(env.getProperty("SPRING_PROFILES_ACTIVE")).isEqualTo("postgres");
    }

    @Test
    void parserBuildsJdbcFromHostParts() {
        String jdbc = PostgresUrlParser.buildJdbcUrl("dpg-internal", "5432", "notes_db");
        assertThat(jdbc).isEqualTo("jdbc:postgresql://dpg-internal:5432/notes_db?sslmode=require");
    }
}
