package org.example.aiservice;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private Long userId;
    private Long donorId;
    private String email;
    private String role;
    private String sessionId;
}

