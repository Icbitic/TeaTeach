package org.bedrock.teateach.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
    }

    @Test
    void shouldSendMessageToKafka() {
        // Given
        String topic = "test-topic";
        String message = "test-message";
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        // When
        kafkaProducerService.send(topic, message);

        // Then
        verify(kafkaTemplate).send(eq(topic), eq(message));
    }

    @Test
    void shouldHandleEmptyMessage() {
        // Given
        String topic = "test-topic";
        String message = "";
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        // When
        kafkaProducerService.send(topic, message);

        // Then
        verify(kafkaTemplate).send(eq(topic), eq(message));
    }

    @Test
    void shouldHandleEmptyTopic() {
        // Given
        String topic = "";
        String message = "test-message";
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        // When
        kafkaProducerService.send(topic, message);

        // Then
        verify(kafkaTemplate).send(eq(topic), eq(message));
    }
}
