package org.bedrock.teateach.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OllamaConfiguration.class)
public class ApplicationConfig {
    // Additional application-wide configuration can be added here
}
