package org.example.bloodrequestservice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/templates")
@RequiredArgsConstructor
@Slf4j
public class TemplateController {

    private final TemplateService templateService;
    private final BloodRequestService bloodRequestService;

    @GetMapping
    public String listTemplates(@RequestParam Long medcenterId,
                                HttpServletRequest request,
                                Model model) {

        log.info("=== TEMPLATES REQUEST START ===");
        log.info("medcenterId: {}", medcenterId);

        // Получаем параметры пользователя из атрибутов запроса
        String userEmail = (String) request.getAttribute("userEmail");
        String userRole = (String) request.getAttribute("userRole");
        Long userId = (Long) request.getAttribute("userId");
        String token = request.getParameter("token");

        log.info("User info - Email: {}, Role: {}, UserId: {}", userEmail, userRole, userId);
        log.info("Token from query: {}", token);

//        try {
            // Получаем медицинский центр
            String medcenterName = bloodRequestService.getMedCenterName(medcenterId);
            log.info("Medical center name: {}", medcenterName);

            // Получаем шаблоны
            List<Template> templates = templateService.getAll(medcenterId);
            log.info("Found {} templates for medcenterId: {}", templates.size(), medcenterId);

            model.addAttribute("templates", templates);
            model.addAttribute("medcenterId", medcenterId);
            model.addAttribute("medcenterName", medcenterName);
            model.addAttribute("token", token != null ? token : "");
            model.addAttribute("userId", userId != null ? userId : 0L);
            model.addAttribute("role", userRole != null ? userRole : "");
            model.addAttribute("email", userEmail != null ? userEmail : "");

            log.info("=== TEMPLATES REQUEST SUCCESS ===");
            return "templates-list";

//        } catch (Exception e) {
//            log.error("Error in listTemplates: ", e);
//            model.addAttribute("error", "Error loading templates: " + e.getMessage());
//            return "error";
//        }
    }

    @GetMapping("/new")
    public String createTemplate(@RequestParam Long medcenterId,
                                 HttpServletRequest request,
                                 Model model) {

        // Получаем параметры пользователя
        String userEmail = (String) request.getAttribute("userEmail");
        String userRole = (String) request.getAttribute("userRole");
        Long userId = (Long) request.getAttribute("userId");
        String token = request.getParameter("token");

        Template template = new Template();
        template.setMedcenterId(medcenterId);

        model.addAttribute("template", template);
        model.addAttribute("medcenterId", medcenterId);
        model.addAttribute("medcenterName", bloodRequestService.getMedCenterName(medcenterId));
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", userRole);
        model.addAttribute("email", userEmail);

        return "template-form";
    }

    @PostMapping("/save")
    public String saveTemplate(@ModelAttribute Template template,
                               @RequestParam(required = false) String token,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        // Получаем параметры пользователя
        String userEmail = (String) request.getAttribute("userEmail");
        String userRole = (String) request.getAttribute("userRole");
        Long userId = (Long) request.getAttribute("userId");

        try {
            templateService.save(template);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Template saved successfully!");

            // Формируем URL с параметрами
            String redirectUrl = String.format("redirect:/templates?medcenterId=%d%s%s%s%s",
                    template.getMedcenterId(),
                    token != null ? "&token=" + token : "",
                    userId != null ? "&userId=" + userId : "",
                    userRole != null ? "&role=" + userRole : "",
                    userEmail != null ? "&email=" + userEmail : "");

            return redirectUrl;
        } catch (Exception e) {
            log.error("Error saving template: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error saving template: " + e.getMessage());

            String redirectUrl = String.format("redirect:/templates/new?medcenterId=%d%s%s%s%s",
                    template.getMedcenterId(),
                    token != null ? "&token=" + token : "",
                    userId != null ? "&userId=" + userId : "",
                    userRole != null ? "&role=" + userRole : "",
                    userEmail != null ? "&email=" + userEmail : "");

            return redirectUrl;
        }
    }

    @PostMapping("/{id}/create-request")
    public String createRequestFromTemplate(@PathVariable Long id,
                                            @RequestParam Long medcenterId,
                                            @RequestParam(required = false) String token,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {
        try {
            BloodRequest requestObj = templateService.createFromTemplate(id, medcenterId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Request created successfully from template!");

            // Получаем параметры пользователя
            String userEmail = (String) request.getAttribute("userEmail");
            String userRole = (String) request.getAttribute("userRole");
            Long userId = (Long) request.getAttribute("userId");

            return String.format("redirect:/requests/%d%s%s%s%s",
                    requestObj.getBlood_request_id(),
                    token != null ? "?token=" + token : "",
                    userId != null ? "&userId=" + userId : "",
                    userRole != null ? "&role=" + userRole : "",
                    userEmail != null ? "&email=" + userEmail : "");

        } catch (Exception e) {
            log.error("Error creating request from template: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating request: " + e.getMessage());

            // Получаем параметры пользователя
            String userEmail = (String) request.getAttribute("userEmail");
            String userRole = (String) request.getAttribute("userRole");
            Long userId = (Long) request.getAttribute("userId");

            return String.format("redirect:/templates?medcenterId=%d%s%s%s%s",
                    medcenterId,
                    token != null ? "&token=" + token : "",
                    userId != null ? "&userId=" + userId : "",
                    userRole != null ? "&role=" + userRole : "",
                    userEmail != null ? "&email=" + userEmail : "");
        }
    }

    @GetMapping("/{id}/edit")
    public String editTemplate(@PathVariable Long id,
                               @RequestParam Long medcenterId,
                               @RequestParam(required = false) String token,
                               HttpServletRequest request,
                               Model model) {

        var template = templateService.findById(id);
        if (template.isEmpty()) {
            // Получаем параметры пользователя
            String userEmail = (String) request.getAttribute("userEmail");
            String userRole = (String) request.getAttribute("userRole");
            Long userId = (Long) request.getAttribute("userId");

            return String.format("redirect:/templates?medcenterId=%d%s%s%s%s",
                    medcenterId,
                    token != null ? "&token=" + token : "",
                    userId != null ? "&userId=" + userId : "",
                    userRole != null ? "&role=" + userRole : "",
                    userEmail != null ? "&email=" + userEmail : "");
        }

        // Получаем параметры пользователя
        String userEmail = (String) request.getAttribute("userEmail");
        String userRole = (String) request.getAttribute("userRole");
        Long userId = (Long) request.getAttribute("userId");

        model.addAttribute("template", template.get());
        model.addAttribute("medcenterId", medcenterId);
        model.addAttribute("medcenterName", bloodRequestService.getMedCenterName(medcenterId));
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", userRole);
        model.addAttribute("email", userEmail);

        return "template-form";
    }

    @PostMapping("/{id}/update")
    public String updateTemplate(@PathVariable Long id,
                                 @ModelAttribute Template template,
                                 @RequestParam(required = false) String token,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            template.setId(id);
            templateService.save(template);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Template updated successfully!");

            // Получаем параметры пользователя
            String userEmail = (String) request.getAttribute("userEmail");
            String userRole = (String) request.getAttribute("userRole");
            Long userId = (Long) request.getAttribute("userId");

            return String.format("redirect:/templates?medcenterId=%d%s%s%s%s",
                    template.getMedcenterId(),
                    token != null ? "&token=" + token : "",
                    userId != null ? "&userId=" + userId : "",
                    userRole != null ? "&role=" + userRole : "",
                    userEmail != null ? "&email=" + userEmail : "");

        } catch (Exception e) {
            log.error("Error updating template: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating template: " + e.getMessage());

            // Получаем параметры пользователя
            String userEmail = (String) request.getAttribute("userEmail");
            String userRole = (String) request.getAttribute("userRole");
            Long userId = (Long) request.getAttribute("userId");

            return String.format("redirect:/templates/%d/edit?medcenterId=%d%s%s%s%s",
                    id, template.getMedcenterId(),
                    token != null ? "&token=" + token : "",
                    userId != null ? "&userId=" + userId : "",
                    userRole != null ? "&role=" + userRole : "",
                    userEmail != null ? "&email=" + userEmail : "");
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTemplate(@PathVariable Long id,
                                 @RequestParam Long medcenterId,
                                 @RequestParam(required = false) String token,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            templateService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Template deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting template: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting template: " + e.getMessage());
        }

        // Получаем параметры пользователя
        String userEmail = (String) request.getAttribute("userEmail");
        String userRole = (String) request.getAttribute("userRole");
        Long userId = (Long) request.getAttribute("userId");

        return String.format("redirect:/templates?medcenterId=%d%s%s%s%s",
                medcenterId,
                token != null ? "&token=" + token : "",
                userId != null ? "&userId=" + userId : "",
                userRole != null ? "&role=" + userRole : "",
                userEmail != null ? "&email=" + userEmail : "");
    }
}