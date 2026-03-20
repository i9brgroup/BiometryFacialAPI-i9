package com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "cors")
@Getter
@Setter
@NoArgsConstructor
public class CorsConfiguration {

    @Value("${api.service.frontend.base_url}")
    private String DEFAULT_ALLOWED_ORIGIN;
    private List<String> allowedOrigins;
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
    private List<String> allowedHeaders = List.of("*");
    private boolean allowCredentials = true;
    private Long maxAge = 3600L;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        // Use allowedOrigins from properties if set, otherwise use DEFAULT_ALLOWED_ORIGIN
        List<String> origins = (allowedOrigins != null && !allowedOrigins.isEmpty())
            ? allowedOrigins
            : List.of(DEFAULT_ALLOWED_ORIGIN);
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
