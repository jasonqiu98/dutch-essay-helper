package com.jasonqiu98.dutchessayhelper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "ollama")
public class OllamaProperties {
    private String baseUrl;
    private String model;
}
