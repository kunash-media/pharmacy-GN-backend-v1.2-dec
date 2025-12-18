package com.gn.pharmacy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5500",
                        "http://127.0.0.1:5500",
                        "http://localhost:5502",      // ADD THIS
                        "http://127.0.0.1:5502",      // ADD THIS (Your frontend port)
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "http://localhost:8083",
                        "http://127.0.0.1:8083",
                        "https:admin.goodneews.com"

                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}