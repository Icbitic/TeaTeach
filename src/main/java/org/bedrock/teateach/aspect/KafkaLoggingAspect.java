package org.bedrock.teateach.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.bedrock.teateach.services.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class KafkaLoggingAspect {

    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaLoggingAspect(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
        this.objectMapper = new ObjectMapper(); // for JSON serialization
    }

    @Pointcut("execution(* org.bedrock.teateach.controllers..*(..))")
    public void allControllerMethods() {
    }

    @Around("allControllerMethods()")
    public Object logAndSendToKafka(ProceedingJoinPoint joinPoint) throws Throwable {
        // Collect request data
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        String payload = "N/A";
        try {
            payload = objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            payload = Arrays.toString(args);
        }

        String message = String.format("Method: %s | Payload: %s", methodName, payload);
        kafkaProducerService.send("api-logs", message);

        // Continue actual method execution
        return joinPoint.proceed();
    }
}

