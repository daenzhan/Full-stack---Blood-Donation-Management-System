package org.example.aiservice;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatPageController {
    @GetMapping("/chat")
    public String chatPage(Model model) {
        return "ai-chat";
    }
}
