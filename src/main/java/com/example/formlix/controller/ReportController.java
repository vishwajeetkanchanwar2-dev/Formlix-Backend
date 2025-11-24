package com.example.formlix.controller;

import com.example.formlix.model.User;
import com.example.formlix.repository.Userrepo;
import com.example.formlix.service.ReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportGenerator reportGenerator;
    private final Userrepo userRepository; // ✅ Add this

    @PostMapping("/generate")
    public ResponseEntity<String> generateReport(@RequestBody Map<String, Object> request) {
        try {
            String topic = (String) request.get("topic");
            String formatType = request.getOrDefault("formatType", "docx").toString();
            Integer pageLimit = request.containsKey("pageLimit")
                    ? Integer.parseInt(request.get("pageLimit").toString())
                    : 15;

            if (topic == null || topic.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Topic is required");
            }

            if (pageLimit < 1 || pageLimit > 20) {
                return ResponseEntity.badRequest()
                        .body("Page limit must be between 1 and 20");
            }

            // ✅ Get current logged in user
            User currentUser = getCurrentUser();

            // ✅ Pass user to report generator
            String result = reportGenerator.generateFromTopic(topic, formatType, pageLimit, currentUser);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error generating report: " + e.getMessage());
        }
    }

    @PostMapping("/generate-from-text")
    public ResponseEntity<String> generateReportFromText(@RequestBody Map<String, Object> request) {
        try {
            String topic = (String) request.get("topic");
            String content = (String) request.get("content");
            String formatType = request.getOrDefault("formatType", "docx").toString();
            Integer pageLimit = request.containsKey("pageLimit")
                    ? Integer.parseInt(request.get("pageLimit").toString())
                    : null;

            if (topic == null || topic.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Topic is required");
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Content is required");
            }

            if (pageLimit != null && (pageLimit < 1 || pageLimit > 20)) {
                return ResponseEntity.badRequest()
                        .body("Page limit must be between 1 and 20");
            }

            // ✅ Get current logged in user
            User currentUser = getCurrentUser();

            // ✅ Pass user to report generator
            String result = reportGenerator.generateFromText(topic, content, formatType, pageLimit, currentUser);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error generating report: " + e.getMessage());
        }
    }

    // ✅ Helper method to get current user
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                String userEmail = authentication.getName();
                return userRepository.findByEmail(userEmail)
                        .orElse(null);
            }
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {


            Path filePath = Paths.get("reports").resolve(filename).normalize();
            File file = filePath.toFile();


            if (!file.exists()) {
                System.out.println("File not found!");
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                System.out.println("Resource not readable!");
                return ResponseEntity.notFound().build();
            }

            String contentType;
            if (filename.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (filename.endsWith(".docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else {
                contentType = "application/octet-stream";
            }

            System.out.println("Sending file with content type: " + contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Report Generator Service is running");
    }
}