package org.example.aiservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {

        log.info("New user request: {}", request.getMessage());

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Введите, пожалуйста, вопрос"));
        }

        String aiAnswer = aiChatService.processMessage(request.getMessage());

        return ResponseEntity.ok(new ChatResponse(aiAnswer));
    }
}
