package com.hrservice.hrservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtConfig.class, AuthServiceConfig.class})
public class EnableConfigurationPropertiesConfig {
}

