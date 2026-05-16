package com.sisol.salud.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "SISOL Salud - API de Gestión de Turnos",
        version = "1.0",
        description = "Documentación interactiva de los endpoints para el sistema inteligente de turnos de SISOL Salud.",
        contact = @Contact(
            name = "Soporte SISOL",
            email = "soporte@sisolsalud.pe"
        )
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Ingrese su token JWT obtenido del login para acceder a los endpoints protegidos."
)
public class OpenApiConfig {
}
