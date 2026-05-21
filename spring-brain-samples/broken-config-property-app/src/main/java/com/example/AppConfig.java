package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {

    @Value("${app.secret}")
    private String secret;

    @Value("${app.timeout}")
    private int timeout;
}
