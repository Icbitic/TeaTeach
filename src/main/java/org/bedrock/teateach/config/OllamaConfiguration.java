package org.bedrock.teateach.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OllamaConfiguration {

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.model:llama3}")
    private String ollamaModel;

    @Bean
    public ChatClient.Builder chatClientBuilder(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .defaultSystem("You are an intelligent educational AI assistant for the TeaTeach platform. " +
                        "Your role is to analyze educational content, provide structured insights, and help improve learning outcomes. " +
                        "Always provide detailed, evidence-based responses, formatted appropriately for the requested task.");
    }
}
