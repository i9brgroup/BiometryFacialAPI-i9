package com.i9brgroup.jbarreto.facial_auth_i9.infra.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfigurations {

    @Bean
    public OpenAPI customDoc() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .info(new Info().title("API de controle do sistema facial I9BRGroup")
                        .version("0.0.2")
                        .description("Esse sistema é responsável por gerenciar a autenticação de usuários, multi-tenant e acesso a dados de Employees. Ele oferece uma API RESTful para realizar operações relacionadas à autenticação, gerenciamento de tenants e acesso a dados de Employees.")
                        .contact(new Contact().name("Junior de Oliveira").email("joselito.barreto@i9brgroup.com")));
    }
}
