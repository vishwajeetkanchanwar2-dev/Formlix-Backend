package com.example.formlix.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // ✅ Only Contact form email method
    public void sendContactFormEmail(String fromEmail, String userMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your-email@gmail.com");
            message.setTo("formlix5@gmail.com"); // ✅ Your company email
            message.setSubject("New Contact Form Message - Formlix 📬");
            message.setText(
                    "You have received a new message from the Formlix contact form:\n\n" +
                            "From: " + fromEmail + "\n\n" +
                            "Message:\n" + userMessage + "\n\n" +
                            "---\n" +
                            "This is an automated message from Formlix Contact Form."
            );

            mailSender.send(message);
            log.info("✅ Contact form email sent from: {}", fromEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send contact form email from {}: {}", fromEmail, e.getMessage());
        }
    }
}
