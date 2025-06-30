package org.bedrock.teateach.controllers;

import org.bedrock.teateach.llm.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class ChatController {

    LLMService llmService;

    @Autowired
    public ChatController(LLMService llmService) {
        this.llmService = llmService;
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody String prompt) {
        return new ResponseEntity<>("[PLACEHOLDER]", HttpStatus.OK);
    }
}
