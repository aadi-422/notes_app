package com.notesapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;
import org.springdoc.core.service.OpenAPIService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenApiController {

    private final OpenAPIService openAPIService;
    private final ObjectMapper objectMapper;

    public OpenApiController(OpenAPIService openAPIService, ObjectMapper objectMapper) {
        this.openAPIService = openAPIService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/openapi.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unchecked")
    public Map<String, Object> openApiDocument(HttpServletRequest request) throws JsonProcessingException {
        Locale locale = request.getLocale() != null ? request.getLocale() : Locale.getDefault();
        OpenAPI openAPI = openAPIService.build(locale);
        String json = objectMapper.writeValueAsString(openAPI);
        return objectMapper.readValue(json, Map.class);
    }
}
