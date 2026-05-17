package com.notesapp.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sqlite")
public class SqliteDirectoryInitializer implements ApplicationRunner {

    @Value("${SQLITE_PATH:./data/notes.db}")
    private String sqlitePath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path dbPath = Paths.get(sqlitePath).toAbsolutePath().normalize();
        Path parent = dbPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
