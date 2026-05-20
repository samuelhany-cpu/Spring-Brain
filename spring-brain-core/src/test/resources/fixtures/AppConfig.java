package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.timeout}")
    private int timeout;

    @Value("${jwt.secret}")
    private String jwtSecret;
}
