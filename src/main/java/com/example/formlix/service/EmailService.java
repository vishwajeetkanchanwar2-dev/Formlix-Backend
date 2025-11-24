package com.example.formlix.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendRegistrationEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your-email@gmail.com");
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
            System.out.println("✅ Registration email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send registration email: " + e.getMessage());
        }
    }

    public void sendLoginEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your-email@gmail.com");
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
            System.out.println("✅ Login email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send login email: " + e.getMessage());
        }
    }

    // ✅ New method for Contact/Footer form
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
            System.out.println("✅ Contact form email sent from: " + fromEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send contact form email: " + e.getMessage());
        }
    }
}