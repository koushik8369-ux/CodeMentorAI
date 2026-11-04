package com.codementor.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenRouterConfig {

    @Value("${openrouter.api.key:}")
    private String apiKey;

    @Value("${openrouter.api.model:tencent/hy4}")
    private String defaultModel;

    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String apiUrl;

    public String getApiKey() {
        return apiKey != null ? apiKey.trim() : "";
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isBlank();
    }

    public String getDefaultModel() {
        return defaultModel != null && !defaultModel.trim().isBlank()
                ? defaultModel.trim()
                : "tencent/hy4";
    }

    public String getApiUrl() {
        return apiUrl != null && !apiUrl.trim().isBlank()
                ? apiUrl.trim()
                : "https://openrouter.ai/api/v1/chat/completions";
    }
}
