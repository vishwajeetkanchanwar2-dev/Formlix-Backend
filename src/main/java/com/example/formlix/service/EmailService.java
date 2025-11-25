package com.example.formlix.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;

    @Async // ✅ Email background me jayega
    public void sendRegistrationEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("formlix5@gmail.com"); // ✅ Your actual email
            message.setTo(toEmail);
            message.setSubject("Welcome to Formlix! 🎉");
            message.setText(
                    "Hi " + userName + ",\n\n" +
                            "Welcome to Formlix! Your account has been successfully created.\n\n" +
                            "Email: " + toEmail + "\n\n" +
                            "Thank you for registering with us!\n\n" +
                            "Best Regards,\n" +
                            "Formlix Team"
            );
            mailSender.send(message);
            log.info("✅ Registration email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send registration email to {}: {}", toEmail, e.getMessage());
            // Don't throw exception - just log it
        }
    }

    @Async
    public void sendLoginEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("formlix5@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Login Alert - Formlix 🔐");
            message.setText(
                    "Hi " + userName + ",\n\n" +
                            "You have successfully logged into your Formlix account.\n\n" +
                            "If this wasn't you, please secure your account immediately.\n\n" +
                            "Best Regards,\n" +
                            "Formlix Team"
            );
            mailSender.send(message);
            log.info("✅ Login email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send login email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendContactFormEmail(String fromEmail, String userMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("formlix5@gmail.com");
            message.setTo("formlix5@gmail.com");
            message.setReplyTo(fromEmail); // ✅ User ko reply kar sakte ho
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
