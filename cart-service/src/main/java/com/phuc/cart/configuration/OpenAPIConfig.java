package com.phuc.cart.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI cartServiceAPI() {
        return new OpenAPI()
                .info(new Info().title("Cart Service API")
                        .description("This is the REST API for Cart Service")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0")))

                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .in(HEADER)
                                .name("Authorization")));
    }
}