package org.bedrock.teateach.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private String token;
    private String password;
}
