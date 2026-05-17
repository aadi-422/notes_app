package com.notesapp.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.notesapp.config.AppProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AboutController {

    private final AppProperties appProperties;

    public AboutController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/about")
    public AboutResponse about() {
        Map<String, String> features = new LinkedHashMap<>();
        features.put(
                "note search and pagination",
                "GET /notes supports search, page, size, and archived filters so large note lists stay fast and easy to browse.");
        features.put(
                "soft delete",
                "DELETE /notes/{id} marks notes as deleted instead of removing rows, preventing accidental permanent data loss.");
        features.put(
                "archived notes",
                "PATCH /notes/{id}/archive lets users hide finished notes without deleting them.");

        return new AboutResponse(
                appProperties.about().name(), appProperties.about().email(), features);
    }

    public record AboutResponse(
            String name,
            String email,
            @JsonProperty("my features") Map<String, String> myFeatures) {}
}
