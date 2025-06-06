package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.model.EmailRequest;
import org.hibernate.sql.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import java.util.Locale;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;

import org.thymeleaf.context.Context;

@CrossOrigin(origins = "*") // Pour accepter les requêtes de frontend
@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        try {
            // Préparer le contexte Thymeleaf avec les données du formulaire
            final Context ctx = new Context(Locale.getDefault());
            ctx.setVariable("senderName", request.getName());
            ctx.setVariable("senderEmail", request.getFrom());
            ctx.setVariable("subject", request.getSubject());
            ctx.setVariable("messageContent", request.getText());

            // Traiter le template avec Thymeleaf
            final String htmlContent = templateEngine.process("email-template", ctx);

            // Créer un message MIME pour envoyer du HTML
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(request.getFrom() != null ? request.getFrom() : "address email pas necessaire ex:email@demo.com");
            helper.setTo(request.getTo() != null ? request.getTo() : "address email qui va recevoir l'email ex: email@demo.com");
            helper.setSubject("[Contact Site] " + request.getSubject());
            helper.setText(htmlContent, true); // true pour indiquer que c'est du HTML

            ClassPathResource logo = new ClassPathResource("static/images/afriq_ai_logo.jpeg");
            helper.addInline("logo_AfriqAI", logo);

            // Envoyer l'email
            emailSender.send(mimeMessage);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Sends a password reset email to the specified admin.
     * 
     * @param adminEmail The admin user's email address
     * @param adminName The admin user's full name
     * @param resetToken The password reset token
     * @param frontendBaseUrl The base URL of the frontend application
     * @return ResponseEntity with success/failure message
     */
    @PostMapping("/send-password-reset")
    public ResponseEntity<String> sendPasswordResetEmail(
            @RequestParam String adminEmail,
            @RequestParam String adminName, 
            @RequestParam String resetToken,
            @RequestParam(defaultValue = "http://localhost:5173") String frontendBaseUrl) {
        try {
            // Prepare the reset link
            String resetLink = frontendBaseUrl + "/reset-password?token=" + resetToken;
            
            // Prepare Thymeleaf context with password reset data
            final Context ctx = new Context(Locale.getDefault());
            ctx.setVariable("adminName", adminName);
            ctx.setVariable("adminEmail", adminEmail);
            ctx.setVariable("resetLink", resetLink);
            ctx.setVariable("resetToken", resetToken);

            // Process the password reset template
            final String htmlContent = templateEngine.process("password-reset-email", ctx);

            // Create MIME message for HTML email
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("noreply@afriq-ai.org");
            helper.setTo(adminEmail);
            helper.setSubject("Password Reset Request - AFRIQ'AI Ranking");
            helper.setText(htmlContent, true);

            // Add logo as inline image (optional)
            try {
                ClassPathResource logo = new ClassPathResource("static/images/afriq_ai_logo.jpeg");
                if (logo.exists()) {
                    helper.addInline("logo_AfriqAI", logo);
                }
            } catch (Exception logoException) {
                // Continue without logo if it fails
            }

            // Send the email
            emailSender.send(mimeMessage);
            return ResponseEntity.ok("Password reset email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send password reset email: " + e.getMessage());
        }
    }
}
