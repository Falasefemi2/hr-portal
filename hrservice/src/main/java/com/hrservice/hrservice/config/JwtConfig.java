package com.hrservice.hrservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth-service.jwt")
@Data
public class JwtConfig {
    private String secret;
}

