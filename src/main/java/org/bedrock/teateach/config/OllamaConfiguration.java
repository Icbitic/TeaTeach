package org.bedrock.teateach.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfiguration {

    @Bean
    public ChatClient.Builder chatClientBuilder(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .defaultSystem("You are an intelligent educational AI assistant for the TeaTeach platform. " +
                        "Your role is to analyze educational content, provide structured insights, and help improve learning outcomes. " +
                        "Always provide detailed, evidence-based responses, formatted appropriately for the requested task.");
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // Using in-memory SimpleVectorStore since we're using MySQL (not PostgreSQL)
        // This avoids the PostgreSQL-specific SQL syntax errors
        return SimpleVectorStore.builder(embeddingModel)
                .build();
    }
}
