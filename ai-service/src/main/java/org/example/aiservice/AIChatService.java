package org.example.aiservice;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final GeminiService geminiService;

    public String processMessage(String message) {

        if (!isDonationTopic(message)) {
            return "Я могу отвечать только на вопросы о донорстве крови!";
        }

        return geminiService.generateAnswer(message);
    }

    private boolean isDonationTopic(String msg) {
        String text = msg.toLowerCase();
        return text.contains("кров") ||
                text.contains("донор") ||
                text.contains("гемоглобин") ||
                text.contains("донац") ||
                text.contains("плазм");
    }
}
