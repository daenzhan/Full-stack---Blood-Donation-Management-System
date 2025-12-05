package org.example.bloodrequestservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateApiController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<?> createTemplate(@RequestBody Template template) {
        try {
            Template saved = templateService.save(template);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating template: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Template>> getTemplates(@RequestParam Long medcenterId) {
        List<Template> templates = templateService.getAll(medcenterId);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTemplate(@PathVariable Long id) {
        return templateService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(@PathVariable Long id,
                                            @RequestBody Template template) {
        if (!templateService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        template.setId(id);
        try {
            Template updated = templateService.save(template);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating template: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        if (!templateService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        try {
            templateService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting template: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/create-request")
    public ResponseEntity<?> createRequestFromTemplate(@PathVariable Long id,
                                                       @RequestParam Long medcenterId) {
        try {
            BloodRequest request = templateService.createFromTemplate(id, medcenterId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating request from template: " + e.getMessage());
        }
    }
}