package com.notesapp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("postgres")
public class PostgresDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(PostgresDataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(Environment env) throws SQLException {
        logEnvironment(env);

        RenderPostgresConfigResolver.PostgresConfig config = RenderPostgresConfigResolver.resolve(env);
        log.info("[notes-api] PostgreSQL datasource from {}", config.source());

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.jdbcUrl());
        hikari.setUsername(config.username());
        hikari.setPassword(config.password());
        hikari.setDriverClassName("org.postgresql.Driver");
        hikari.setMaximumPoolSize(5);
        hikari.setConnectionTimeout(60_000);
        hikari.setInitializationFailTimeout(120_000);

        HikariDataSource dataSource = new HikariDataSource(hikari);
        try (Connection connection = dataSource.getConnection()) {
            log.info("[notes-api] PostgreSQL connection OK");
        } catch (SQLException ex) {
            dataSource.close();
            throw new IllegalStateException(
                    "Cannot connect to PostgreSQL (%s). Check notes-db is linked and available. Cause: %s"
                            .formatted(config.source(), ex.getMessage()),
                    ex);
        }
        return dataSource;
    }

    private static void logEnvironment(Environment env) {
        log.info(
                "[notes-api] env: DB_HOST={}, DB_PORT={}, DB_NAME={}, DB_USER={}, DATABASE_URL={}",
                env.getProperty("DB_HOST", "<not set>"),
                env.getProperty("DB_PORT", "<not set>"),
                env.getProperty("DB_NAME", "<not set>"),
                env.getProperty("DB_USER", "<not set>"),
                env.getProperty("DATABASE_URL") != null ? "<set>" : "<not set>");
    }
}
