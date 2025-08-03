package org.kuraterut.authservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Marketplace Auth Service",
                version = "1.0",
                description = "API for user authentication in marketplace"
        )
)
public class OpenApiConfig {
}
