package com.example.formlix.controller;

import com.example.formlix.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendContactMessage(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String message = request.get("message");

            // Validation
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email is required"));
            }

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Message is required"));
            }

            // Send email to formlix5@gmail.com
            emailService.sendContactFormEmail(email, message);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Message sent successfully! We'll get back to you soon.",
                    "success", true
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "❌ Failed to send message. Please try again later.",
                            "success", false
                    ));
        }
    }
}