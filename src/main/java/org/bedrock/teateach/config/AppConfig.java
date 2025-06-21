package org.bedrock.teateach.config;

import org.bedrock.teateach.llm.LLMService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // Example of a custom bean for LLM integration
    @Bean
    public LLMService llmService() {
        // In a real application, this would configure and initialize your LLM client
        // e.g., set API keys, choose models, etc.
        return new LLMService("YOUR_LLM_API_KEY");
    }

    // You could also configure other utility beans here,
    // like a file storage service or a notification service.
}
