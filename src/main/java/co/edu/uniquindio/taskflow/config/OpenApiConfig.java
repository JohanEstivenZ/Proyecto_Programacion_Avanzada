package co.edu.uniquindio.taskflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuracion de SpringDoc OpenAPI 3 para Swagger UI.
 * Accesible en: http://localhost:8080/api/v1/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskFlow API — Gestion de Tareas de Equipo")
                        .version("1.0.0")
                        .description("API REST de ejemplo con Spring Boot, JPA, JWT, Spring AI y DDD.")
                        .contact(new Contact()
                                .name("Ingenieria de Sistemas - UQ")
                                .email("ingenieria@uniquindio.edu.co")))
                .servers(List.of(new Server().url("http://localhost:8080/api/v1").description("Local")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
