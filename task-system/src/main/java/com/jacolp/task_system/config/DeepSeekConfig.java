package com.jacolp.task_system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.deepseek")
public class DeepSeekConfig {
    private String apiKey;
    private String apiUrl;
    private String model;
}