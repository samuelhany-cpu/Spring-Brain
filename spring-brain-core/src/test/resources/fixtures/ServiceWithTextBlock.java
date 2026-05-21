package com.example.service;

import org.springframework.stereotype.Service;

@Service
public class ServiceWithTextBlock {

    public String getJson() {
        return """
                {
                    "key": "value"
                }
                """;
    }
}
