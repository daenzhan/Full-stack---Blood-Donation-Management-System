package org.example.userservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }


    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }



    @GetMapping("/home")
    public String homePage(@RequestParam String token,
                           @RequestParam Long userId,
                           @RequestParam String role,
                           @RequestParam String email,
                           Model model) {

        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        return "home";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:http://localhost:8080/";
    }


    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        RedirectAttributes redirectAttributes) {
        try {
            LoginRequest request = new LoginRequest();
            request.setEmail(email);
            request.setPassword(password);

            AuthResponse response = userService.login(request);

            // Добавляем параметры для редиректа на home
            redirectAttributes.addAttribute("token", response.getToken());
            redirectAttributes.addAttribute("userId", response.getUserId());
            redirectAttributes.addAttribute("role", response.getRole());
            redirectAttributes.addAttribute("email", response.getEmail());

            // ВСЕГДА редирект на home после логина
            return "redirect:http://localhost:8080/home";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String role,
                           RedirectAttributes redirectAttributes) {
        try {
            RegisterRequest request = new RegisterRequest();
            request.setEmail(email);
            request.setPassword(password);
            request.setRole(Role.valueOf(role));

            AuthResponse response = userService.register(request);

            // Добавляем параметры для редиректа
            redirectAttributes.addAttribute("token", response.getToken());
            redirectAttributes.addAttribute("userId", response.getUserId());
            redirectAttributes.addAttribute("role", response.getRole());
            redirectAttributes.addAttribute("email", response.getEmail());

            // Редирект в зависимости от роли
            if (response.getRole() == Role.DONOR) {
                return "redirect:http://localhost:8080/donor/complete-profile";
            } else if (response.getRole() == Role.MED_CENTER) {
                return "redirect:http://localhost:8080/med-center/complete-profile";
            } else {
                // Админы идут сразу на home
                return "redirect:http://localhost:8080/home";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
}
