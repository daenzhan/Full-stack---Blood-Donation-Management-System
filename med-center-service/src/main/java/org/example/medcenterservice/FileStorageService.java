package org.example.medcenterservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/licenses/}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "pdf");

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            if (uploadDir == null) {
                uploadDir = "uploads/licenses/";
            }

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + e.getMessage(), e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            String fileError = validateFile(file);
            if (fileError != null) {
                throw new RuntimeException(fileError);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;

            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("File saved: " + filePath.toAbsolutePath());
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file: " + ex.getMessage());
        }
    }

    private String validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            return "File size exceeds 5MB limit!";
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "Invalid file format! Allowed formats: JPG, PNG, PDF";
        }
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_FORMATS.contains(fileExtension)) {
            return "Invalid file format! Allowed formats: JPG, PNG, PDF";
        }

        return null;
    }

    public void deleteFile(String fileName) {
        try {
            if (fileName != null) {
                Path path = Paths.get(uploadDir).resolve(fileName);
                Files.deleteIfExists(path);
                System.out.println("File deleted: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + e.getMessage());
        }
    }

    public String determineFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "application/octet-stream";
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            default: return "application/octet-stream";
        }
    }
    public String getUploadDir() {
        return uploadDir;
    }
}
