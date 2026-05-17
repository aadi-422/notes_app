package com.notesapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notesOpenApi(AppProperties appProperties) {
        final String bearerScheme = "bearerAuth";
        String contactName = "Notes API";
        String contactEmail = "support@example.com";
        if (appProperties.about() != null) {
            contactName = appProperties.about().name();
            contactEmail = appProperties.about().email();
        }
        return new OpenAPI()
                .info(new Info()
                        .title("Notes API")
                        .version("1.0.0")
                        .description("Multi-user secure notes backend with JWT authentication and note sharing.")
                        .contact(new Contact().name(contactName).email(contactEmail)))
                .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
                .components(new Components()
                        .addSecuritySchemes(
                                bearerScheme,
                                new SecurityScheme()
                                        .name(bearerScheme)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
