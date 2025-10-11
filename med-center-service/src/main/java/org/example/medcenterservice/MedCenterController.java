package org.example.medcenterservice;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/medcenters")
public class MedCenterController {

    private final MedCenterService medCenterService;
    private final String upload_dir = "uploads/licenses/";

    private static final long max_file_size = 5 * 1024 * 1024; // 5MB
    private static final List<String> allowed_format = Arrays.asList("jpg", "jpeg", "png", "pdf");

    public MedCenterController(MedCenterService medCenterService) throws IOException {
        this.medCenterService = medCenterService;
        Files.createDirectories(Paths.get(upload_dir));
    }
    @GetMapping("/complete-profile")
    public String showCompleteProfilePage(@RequestParam String token,
                                          @RequestParam Long userId,
                                          @RequestParam String role,
                                          @RequestParam String email,Model model) {
        try {
            MedCenter existingProfile = medCenterService.getProfileByUserId(userId);
            if (existingProfile != null) {
                return "redirect:http://localhost:8080/home?token=" + token +
                        "&userId=" + userId + "&role=" + role + "&email=" + email +
                        "&info=Profile already completed";
            }
        } catch (Exception e) {
        }
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        return "med-center-complete-profile";
    }

    @PostMapping("/complete-profile")
    public String completeProfile(@RequestParam("name") String name,
                                  @RequestParam("location") String location,
                                  @RequestParam("phone") String phone,
                                  @RequestParam("specialization") String specialization,
                                  @RequestParam("directorName") String directorName,
                                  @RequestParam("centerEmail") String centerEmail,
                                  @RequestParam(value = "licenseFile", required = false) MultipartFile licenseFile,
                                  @RequestParam("token") String token,
                                  @RequestParam("userId") Long userId,
                                  @RequestParam("role") String role,
                                  @RequestParam("userEmail") String userEmail,
                                  RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Completing profile for user: " + userId);

            String license_file = null;
            if (licenseFile != null && !licenseFile.isEmpty()) {
                String fileError = determine_error_file(licenseFile);
                if (fileError != null) {
                    redirectAttributes.addFlashAttribute("error", fileError);
                    return "redirect:/med-center/complete-profile?token=" + token +
                            "&userId=" + userId + "&role=" + role + "&email=" + userEmail;
                }
                license_file = save_uploaded_file(licenseFile);
                System.out.println("License file saved: " + license_file);
            }

            MedCenter profile = medCenterService.completeProfile(
                    userId, name, location, phone, specialization,
                    directorName, centerEmail, license_file
            );

            System.out.println("Medical center profile created with ID: " + profile.getMed_center_id());

            return "redirect:http://localhost:8080/home?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + userEmail +
                    "&success=Medical center profile completed successfully!";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error completing profile: " + e.getMessage());
            return "redirect:/med-center/complete-profile?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + userEmail;
        }
    }

    @GetMapping
    public String list_page(Model model) {
        model.addAttribute("medCenters", medCenterService.get_all());
        return "list";
    }

    @GetMapping("/new")
    public String create_page(Model model) {
        model.addAttribute("medCenter", new MedCenter());
        return "create";
    }

    @PostMapping("/save")
    public String save(@RequestParam String name,
                       @RequestParam String location,
                       @RequestParam String phone,
                       @RequestParam String specialization,
                       @RequestParam Long user_id,
                       @RequestParam(value = "license_file", required = false) MultipartFile file,
                       RedirectAttributes redirectAttributes) {

        MedCenter medCenter = new MedCenter();
        medCenter.setName(name);
        medCenter.setLocation(location);
        medCenter.setPhone(phone);
        medCenter.setSpecialization(specialization);
        medCenter.setUser_id(user_id);

        if (file != null && !file.isEmpty()) {
            String file_error = determine_error_file(file);
            if (file_error != null) {
                redirectAttributes.addFlashAttribute("error", file_error);
                return "redirect:/medcenters/new";
            }

            try {
                String file_name = save_uploaded_file(file);
                medCenter.setLicense_file(file_name);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Ошибка при сохранении файла: " + e.getMessage());
                return "redirect:/medcenters/new";
            }
        }
        medCenterService.save(medCenter);
        redirectAttributes.addFlashAttribute("success", "Медицинский центр успешно создан");
        return "redirect:/medcenters";
    }

    @GetMapping("/{id}")
    public String detail_page (@PathVariable Long id, Model model) {
        MedCenter medCenter = medCenterService.get_by_id(id);
        if (medCenter != null) {
            model.addAttribute("medCenter", medCenter);
            return "view";
        }
        return "redirect:/medcenters";
    }

    @GetMapping("/client/{medcenter_id}")
    public ResponseEntity<MedCenter> get_medcenter_by_id(@PathVariable("medcenter_id") Long medcenter_id) {
        MedCenter m = medCenterService.get_by_id(medcenter_id);
        if (m != null) {
            return ResponseEntity.ok(m);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/client/search")
    public ResponseEntity<List<MedCenter>> search_by_name(@RequestParam String name) {
        List<MedCenter> medCenters = medCenterService.searchByName(name);
        return ResponseEntity.ok(medCenters);
    }

    @GetMapping("/{id}/edit")
    public String update_page (@PathVariable Long id, Model model) {
        MedCenter medCenter = medCenterService.get_by_id(id);
        if (medCenter != null) {
            model.addAttribute("medCenter", medCenter);
            return "edit";
        }
        return "redirect:/medcenters";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String name,
                       @RequestParam String location,
                       @RequestParam String phone,
                       @RequestParam String specialization,
                       @RequestParam Long user_id,
                       @RequestParam(value = "license_file", required = false) MultipartFile file,
                       RedirectAttributes redirectAttributes) {
        try {
            MedCenter existing_medCenter = medCenterService.get_by_id(id);
            if (existing_medCenter != null) {
                MedCenter new_medCenter = new MedCenter();
                new_medCenter.setMed_center_id(id);
                new_medCenter.setName(name);
                new_medCenter.setLocation(location);
                new_medCenter.setPhone(phone);
                new_medCenter.setSpecialization(specialization);
                new_medCenter.setUser_id(user_id);

                if (file != null && !file.isEmpty()) {
                    String file_error = determine_error_file(file);
                    if (file_error != null) {
                        redirectAttributes.addFlashAttribute("error", file_error);
                        return "redirect:/medcenters/" + id + "/edit";
                    }

                    // Удаляем старый файл, если он существует
                    if (existing_medCenter.getLicense_file() != null) {
                        delete_file(existing_medCenter.getLicense_file());
                    }

                    String file_name = save_uploaded_file(file);
                    new_medCenter.setLicense_file(file_name); // Исправлено имя переменной
                } else {
                    // если вдруг файл не загружен
                    new_medCenter.setLicense_file(existing_medCenter.getLicense_file());
                }

                medCenterService.update(id, new_medCenter);
                redirectAttributes.addFlashAttribute("success", "Медицинский центр успешно обновлен");
            }
            return "redirect:/medcenters";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении: " + e.getMessage());
            return "redirect:/medcenters/" + id + "/edit";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteMedCenter(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            MedCenter medCenter = medCenterService.get_by_id(id);
            if (medCenter != null && medCenter.getLicense_file() != null) {
                delete_file(medCenter.getLicense_file()); // Исправлено имя метода
            }
            medCenterService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Медицинский центр успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/medcenters";
    }

    @GetMapping("/{id}/license")
    public String showLicense(@PathVariable Long id, Model model) {
        MedCenter medCenter = medCenterService.get_by_id(id);
        if (medCenter != null) {
            model.addAttribute("medCenter", medCenter);
            return "license-view";
        }
        return "redirect:/medcenters";
    }

    // чтобы скачать файл
    @GetMapping("/{id}/license/download")
    public ResponseEntity<Resource> download_license(@PathVariable Long id) {
        try {
            MedCenter medCenter = medCenterService.get_by_id(id);
            if (medCenter == null || medCenter.getLicense_file() == null) {
                return ResponseEntity.notFound().build();
            }

            Path file_path = Paths.get(upload_dir).resolve(medCenter.getLicense_file()).normalize();
            Resource resource = new UrlResource(file_path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String file_type = determine_file_type(medCenter.getLicense_file());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file_type))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + medCenter.getLicense_file() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // чтобы отобразить файл по имени
    @GetMapping("/uploads/licenses/{file_name:.+}")
    public ResponseEntity<Resource> access_file(@PathVariable String file_name) {
        try {
            Path file_path = Paths.get(upload_dir).resolve(file_name).normalize();
            Resource resource = new UrlResource(file_path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String file_type = determine_file_type(file_name);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file_type))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file_name + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determine_file_type(String file_name) {
        String extension = file_name.substring(file_name.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            default:
                return "application/octet-stream";
        }
    }

    private String determine_error_file(MultipartFile file) {
        if (file.getSize() > max_file_size) {
            return "размер файла более 5MB!";
        }
        String original_file_name = file.getOriginalFilename(); // Исправлено имя переменной
        if (original_file_name == null || !original_file_name.contains(".")) {
            return "Неправильный формат! Разрешенный формат: .jpg, .png, .pdf";
        }

        String fileExtension = original_file_name.substring(original_file_name.lastIndexOf(".") + 1).toLowerCase();
        if (!allowed_format.contains(fileExtension)) {
            return "Неправильный формат! Разрешенный формат: .jpg, .png, .pdf";
        }
        return null;
    }

    private String save_uploaded_file(MultipartFile file) throws IOException {
        String original_file_name = file.getOriginalFilename();
        String fileExtension = "";
        if (original_file_name != null && original_file_name.contains(".")) {
            fileExtension = original_file_name.substring(original_file_name.lastIndexOf("."));
        }
        String file_name = UUID.randomUUID().toString() + fileExtension;

        Path path = Paths.get(upload_dir + file_name);
        Files.copy(file.getInputStream(), path);

        return file_name;
    }

    private void delete_file(String fileName) {
        try {
            Path path = Paths.get(upload_dir + fileName);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}