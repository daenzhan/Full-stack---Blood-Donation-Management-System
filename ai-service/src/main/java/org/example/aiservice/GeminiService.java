package org.example.aiservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.base-url}")
    private String baseUrl;

    @Value("${ai.api.model}")
    private String model;

    @Value("${ai.system.prompt}")
    private String systemPrompt;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateAnswer(String userMessage) {
        log.info("➡ Sending request to Gemini...");

        String url = String.format(
                "%s/models/%s:generateContent?key=%s",
                baseUrl, model, apiKey
        );


        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(
                                Map.of("text", systemPrompt)
                        )
                ),
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", userMessage)
                                )
                        )
                )
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.debug(" Gemini response: {}", response.getBody());

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("candidates")) {
                log.error(" Gemini: empty or invalid response");
                return "Ошибка обработки ответа от ИИ.";
            }

            Map<String, Object> candidate =
                    (Map<String, Object>) ((List<?>) body.get("candidates")).get(0);

            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");

            return parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Gemini error: {}", e.toString());
            return "Ошибка при генерации ответа. Попробуйте позже!";
        }
    }
}
