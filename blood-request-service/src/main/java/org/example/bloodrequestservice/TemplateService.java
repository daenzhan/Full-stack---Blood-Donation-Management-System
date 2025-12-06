package org.example.bloodrequestservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Slf4j

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final BloodRequestRepository bloodRequestRepository;

    public Template save(Template template) {
        return templateRepository.save(template);
    }

//    public List<Template> getAll(Long medcenterId) {
//        return templateRepository.findByMedcenterId(medcenterId);
//    }

    public List<Template> getAll(Long medcenterId) {
        log.info("Getting templates for medcenterId: {}", medcenterId);

        try {
            List<Template> templates = templateRepository.findByMedcenterId(medcenterId);
            log.info("Found {} templates in database", templates.size());

            if (templates.isEmpty()) {
                log.info("No templates found, creating a test template for debugging");
                // Создаем тестовый шаблон для отладки
                Template testTemplate = new Template();
                testTemplate.setMedcenterId(medcenterId);
                testTemplate.setName("Test Template");
                testTemplate.setComponentType("Plasma");
                testTemplate.setBloodGroup("A");
                testTemplate.setRhesusFactor("+");
                testTemplate.setVolume(2);
                testTemplate.setComments("Test template for debugging");

                templates = List.of(testTemplate);
            }

            return templates;

        } catch (Exception e) {
            log.error("Error in getAll templates: ", e);
            throw new RuntimeException("Failed to get templates: " + e.getMessage(), e);
        }
    }

    public Optional<Template> findById(Long id) {
        return templateRepository.findById(id);
    }

    public void delete(Long id) {
        templateRepository.deleteById(id);
    }

    public BloodRequest createFromTemplate(Long templateId, Long medCenterId) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

        BloodRequest request = new BloodRequest();
        request.setComponent_type(template.getComponentType());
        request.setBlood_group(template.getBloodGroup());
        request.setRhesus_factor(template.getRhesusFactor());
        request.setVolume(String.valueOf(template.getVolume()));
        request.setComments(template.getComments());
        request.setMedcenter_id(medCenterId);
        request.setDeadline(LocalDateTime.now().plusDays(3));

        return bloodRequestRepository.save(request);
    }
}